/**
 * نوروتور — اطلاع‌رسانی فرم‌ها به تلگرام از طریق «رلهٔ سروری»
 *
 * ⚠️ این فایل دیگر مثل قبل مستقیم به `api.telegram.org` وصل نمی‌شود:
 *    - `api.telegram.org` از ایران فیلتر است و ارسال از مرورگر بازدیدکننده هرگز نمی‌رسید.
 *    - توکن ربات هم نباید داخل مرورگر باشد.
 * اکنون درخواست به وب‌اپ Google Apps Script (رله) می‌رود و سرور گوگل پیام را به تلگرام می‌دهد:
 *
 *     فرم سایت → <نشانی رله> (/exec) → سرور گوگل → api.telegram.org → گروه تلگرام
 *
 * استفاده (پیشنهادی: به‌جای این فایل، همان ماژول اصلی را لود کنید):
 *   <script src="telegram-integration.js"></script>
 *
 * در index.html فعلی، خودِ فرم از تابع `sendLeadToRelay` استفاده می‌کند و این فایل
 * فقط برای سازگاری با صفحه‌های قدیمی نگه داشته شده است.
 */

(function () {
  'use strict';

  // نشانی رله: همان مقدار `leadEndpoint` در index.html
  var RELAY_URL =
    (typeof window !== 'undefined' && String(window.NOUROTOUR_RELAY_URL || '').trim()) ||
    'https://script.google.com/macros/s/AKfycbxm7nnIkic08wYopqEZxxgwpO6YliSXviZr2GMHDh2PnhvJhJpUxPNe8ux0aAhaRkee/exec';

  /**
   * ارسال درخواست فرم به رلهٔ سروری (سرور گوگل پیام را به تلگرام می‌رساند).
   * @param {Object} leadData اطلاعات فرم
   * @returns {Promise<boolean>} موفقیت ارسال
   */
  async function sendToTelegram(leadData) {
    if (!RELAY_URL) {
      console.warn('[Nourotour relay] نشانی رله تنظیم نشده است');
      return false;
    }

    var payload = Object.assign({ site: 'nourotour.ir' }, leadData || {});
    var controller = typeof AbortController === 'function' ? new AbortController() : null;
    var timer = controller ? setTimeout(function () { controller.abort(); }, 15000) : null;

    try {
      var response = await fetch(RELAY_URL, {
        method: 'POST',
        // text/plain = درخواست ساده (بدون preflight)؛ Apps Script از مرورگر همین را می‌پذیرد.
        headers: { 'Content-Type': 'text/plain;charset=utf-8' },
        body: JSON.stringify(payload),
        redirect: 'follow',
        signal: controller ? controller.signal : undefined
      });

      var text = await response.text();
      var data = null;

      try { data = JSON.parse(text); } catch (ignored) {}

      if (response.ok && data && data.status === 'ok') {
        console.log('✅ پیام از طریق سرور گوگل به تلگرام رسید');
        return true;
      }

      console.error('[Nourotour relay] ارسال ناموفق:', (data && data.message) || text || ('HTTP ' + response.status));
      return false;
    } catch (error) {
      console.error('[Nourotour relay] خطا در تماس با رله:', error);
      return false;
    } finally {
      if (timer) clearTimeout(timer);
    }
  }

  // اگر صفحه از ماژول اصلی استفاده می‌کند، آن را ترجیح می‌دهیم (یک مسیر ارسال، بدون پیام تکراری).
  window.NourotourTelegram = window.NourotourTelegram || {
    send: sendToTelegram,
    version: 'relay-compat'
  };

  window.sendToTelegram = sendToTelegram;

  console.log('ℹ️ telegram-notifier.js اکنون فقط کلاینت رلهٔ سروری است (بدون توکن و بدون تماس مستقیم با تلگرام)');
})();
