/**
 * نوروتور — کلاینت رلهٔ سروری تلگرام (نسخه ۲)
 *
 * ⚠️ تغییر مهم نسبت به نسخهٔ ۱: این فایل دیگر از مرورگر به api.telegram.org وصل نمی‌شود؛
 * چون api.telegram.org از ایران فیلتر است و پیام‌ها نمی‌رسیدند. اکنون درخواست به
 * وب‌اپ Google Apps Script (رلهٔ سروری) فرستاده می‌شود و سرور گوگل پیام را به تلگرام می‌دهد.
 * به همین دلیل هم دیگر هیچ توکن تلگرامی داخل مرورگر نیست.
 *
 * مسیر ارسال:  فرم سایت → Apps Script (/exec) → تلگرام
 *
 * استفاده:
 *   <script src="telegram-integration.js"></script>
 *   <script>
 *     await window.NourotourTelegram.send({ kind: 'consult', fullName: '…', phone: '…' });
 *   </script>
 *
 * نشانی رله: مقدار پیش‌فرض پایین همین فایل است؛ اگر خواستید از بیرون عوضش کنید،
 * قبل از بارگذاری این فایل بنویسید:
 *   <script>window.NOUROTOUR_RELAY_URL = "https://script.google.com/macros/s/…/exec";</script>
 */

(function () {
  'use strict';

  // همان نشانی‌ای که در index.html (defaultData.general.leadEndpoint) قرار دارد.
  var DEFAULT_RELAY_URL =
    'https://script.google.com/macros/s/AKfycbxm7nnIkic08wYopqEZxxgwpO6YliSXviZr2GMHDh2PnhvJhJpUxPNe8ux0aAhaRkee/exec';

  var CONFIG = {
    relayUrl: DEFAULT_RELAY_URL,
    timeout: 15000,
    debug: true
  };

  function log(message, type) {
    if (!CONFIG.debug) return;

    var styles = {
      info: 'color: #0088cc; font-weight: bold',
      success: 'color: #00cc88; font-weight: bold',
      error: 'color: #cc0000; font-weight: bold',
      warn: 'color: #cc8800; font-weight: bold'
    };

    console.log('%c[Nourotour relay] ' + message, styles[type] || styles.info);
  }

  /** نشانی رله: اول از window (پنل مدیریت/دستکاری بیرونی)، بعد مقدار پیش‌فرض. */
  function resolveRelayUrl() {
    var override = String(window.NOUROTOUR_RELAY_URL || '').trim();
    return override || CONFIG.relayUrl || '';
  }

  /**
   * ارسال درخواست فرم به رلهٔ سروری.
   * @param {Object} leadData رکورد فرم (kind, fullName, phone, destination, message, …)
   * @returns {Promise<boolean>} true اگر سرور گوگل پیام را به تلگرام رسانده باشد.
   */
  async function send(leadData) {
    var url = resolveRelayUrl();

    if (!url) {
      log('نشانی رله تنظیم نشده است؛ درخواست فقط در مرورگر ذخیره شد', 'warn');
      return false;
    }

    // رکورد را با فیلدهای کمکی (زمان شمسی) کامل می‌کنیم؛ قالب‌بندی پیام در سرور انجام می‌شود.
    var payload = Object.assign({ site: 'nourotour.ir' }, leadData || {}, {
      createdAtFa: (leadData && leadData.createdAtFa) || getPersianDateTime()
    });

    var controller = typeof AbortController === 'function' ? new AbortController() : null;
    var timer = controller ? setTimeout(function () { controller.abort(); }, CONFIG.timeout) : null;

    try {
      log('ارسال درخواست به رلهٔ سروری…', 'info');

      var response = await fetch(url, {
        method: 'POST',
        // text/plain = درخواست ساده (بدون preflight)؛ تنها حالتی که Apps Script از مرورگر می‌پذیرد.
        headers: { 'Content-Type': 'text/plain;charset=utf-8' },
        body: JSON.stringify(payload),
        redirect: 'follow',
        signal: controller ? controller.signal : undefined
      });

      var text = '';
      try { text = await response.text(); } catch (ignored) {}

      var data = null;
      try { data = JSON.parse(text); } catch (ignored) {}

      if (response.ok && data && data.status === 'ok') {
        log('✅ پیام از طریق سرور گوگل به تلگرام رسید', 'success');
        return true;
      }

      var detail = (data && data.message) || text.replace(/\s+/g, ' ').trim().slice(0, 200) || ('HTTP ' + response.status);
      log('❌ ارسال ناموفق: ' + detail, 'error');
      return false;
    } catch (error) {
      var aborted = error && error.name === 'AbortError';
      log('❌ خطا در تماس با رله: ' + (aborted ? 'بدون پاسخ (timeout)' : (error && error.message) || error), 'error');
      return false;
    } finally {
      if (timer) clearTimeout(timer);
    }
  }

  function getPersianDateTime() {
    var now = new Date();

    try {
      var date = now.toLocaleDateString('fa-IR');
      var time = now.toLocaleTimeString('fa-IR', { hour: '2-digit', minute: '2-digit' });
      return date + ' ' + time;
    } catch (ignored) {
      return now.toISOString().replace('T', ' ').substring(0, 19);
    }
  }

  window.NourotourTelegram = {
    send: send,
    config: CONFIG,
    relayUrl: resolveRelayUrl,
    version: '2.0.0'
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
      log('ماژول رلهٔ تلگرام آماده است (ارسال از سمت سرور گوگل انجام می‌شود)', 'success');
    });
  } else {
    log('ماژول رلهٔ تلگرام آماده است (ارسال از سمت سرور گوگل انجام می‌شود)', 'success');
  }
})();
