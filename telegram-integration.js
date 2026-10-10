/**
 * نوروتور - اتصال تلگرام (نسخه نهایی)
 * این فایل را در روت سایت قرار دهید و در index.html لود کنید
 * 
 * نحوه استفاده:
 * <script src="telegram-integration.js"></script>
 */

(function() {
  'use strict';

  // ═══════════════════════════════════════════════════════════════
  // تنظیمات اصلی
  // ═══════════════════════════════════════════════════════════════
  
  const CONFIG = {
    telegram: {
      botToken: '8898159650:AAEqqUDMSx3BOwzITqvq-a-mrXLI-tWIhNE',
      chatId: '-1004463990161'
    },
    debug: true // برای نمایش لاگ در Console
  };

  // ═══════════════════════════════════════════════════════════════
  // توابع کمکی
  // ═══════════════════════════════════════════════════════════════

  /**
   * لاگ با رنگ در Console
   */
  function log(message, type = 'info') {
    if (!CONFIG.debug) return;
    
    const styles = {
      info: 'color: #0088cc; font-weight: bold',
      success: 'color: #00cc88; font-weight: bold',
      error: 'color: #cc0000; font-weight: bold',
      warn: 'color: #cc8800; font-weight: bold'
    };
    
    console.log(`%c[Telegram] ${message}`, styles[type] || styles.info);
  }

  /**
   * Escape HTML برای امنیت
   */
  function escapeHtml(text) {
    if (!text) return '';
    const map = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#039;'
    };
    return String(text).replace(/[&<>"']/g, m => map[m]);
  }

  /**
   * تاریخ و زمان فارسی
   */
  function getPersianDateTime() {
    const now = new Date();
    try {
      const date = now.toLocaleDateString('fa-IR');
      const time = now.toLocaleTimeString('fa-IR', {
        hour: '2-digit',
        minute: '2-digit'
      });
      return `${date} ${time}`;
    } catch (e) {
      // Fallback برای مرورگرهایی که فارسی ساپورت نمی‌کنند
      return now.toISOString().replace('T', ' ').substring(0, 19);
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // فرمت کردن پیام تلگرام
  // ═══════════════════════════════════════════════════════════════

  /**
   * ساخت پیام زیبا برای تلگرام
   */
  function formatTelegramMessage(data) {
    const kindLabels = {
      consult: '🔔 درخواست مشاوره',
      booking: '✈️ درخواست رزرو',
      test: '🧪 ارسال آزمایشی'
    };

    const title = kindLabels[data.kind] || '📋 درخواست جدید';
    let message = `<b>${title}</b>\n━━━━━━━━━━━━━━━━━━━\n\n`;

    // اطلاعات شخصی
    if (data.fullName) {
      message += `👤 <b>نام:</b> ${escapeHtml(data.fullName)}\n`;
    }
    
    if (data.phone) {
      message += `📱 <b>تلفن:</b> <code>${escapeHtml(data.phone)}</code>\n`;
    }

    // اطلاعات سفر
    if (data.tour) {
      message += `🎯 <b>تور:</b> ${escapeHtml(data.tour)}\n`;
    }
    
    if (data.destination) {
      message += `📍 <b>مقصد:</b> ${escapeHtml(data.destination)}\n`;
    }
    
    if (data.travelers) {
      message += `👥 <b>تعداد مسافر:</b> ${escapeHtml(data.travelers)}\n`;
    }
    
    if (data.preferredDate) {
      message += `📅 <b>تاریخ:</b> ${escapeHtml(data.preferredDate)}\n`;
    }
    
    if (data.budget) {
      message += `💰 <b>بودجه:</b> ${escapeHtml(data.budget)}\n`;
    }
    
    if (data.contactMethod) {
      message += `📞 <b>روش تماس:</b> ${escapeHtml(data.contactMethod)}\n`;
    }

    // توضیحات
    if (data.message) {
      message += `\n💬 <b>توضیحات:</b>\n${escapeHtml(data.message)}\n`;
    }

    // اطلاعات تکمیلی
    message += `\n━━━━━━━━━━━━━━━━━━━\n`;
    message += `🕐 <b>زمان:</b> ${data.createdAtFa || getPersianDateTime()}\n`;
    
    if (data.id) {
      message += `🆔 <b>شناسه:</b> <code>${escapeHtml(data.id)}</code>\n`;
    }

    message += `\n🌐 <a href="https://nourotour.ir">nourotour.ir</a>`;

    return message;
  }

  // ═══════════════════════════════════════════════════════════════
  // ارسال به تلگرام
  // ═══════════════════════════════════════════════════════════════

  /**
   * ارسال پیام به گروه تلگرام
   */
  async function sendToTelegram(leadData) {
    try {
      log('در حال ارسال به تلگرام...', 'info');
      
      const message = formatTelegramMessage(leadData);
      const url = `https://api.telegram.org/bot${CONFIG.telegram.botToken}/sendMessage`;

      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          chat_id: CONFIG.telegram.chatId,
          text: message,
          parse_mode: 'HTML',
          disable_web_page_preview: true
        })
      });

      const result = await response.json();
      
      if (!result.ok) {
        throw new Error(result.description || 'خطای ناشناخته تلگرام');
      }

      log('✅ پیام با موفقیت به تلگرام ارسال شد', 'success');
      return true;
      
    } catch (error) {
      log(`❌ خطا در ارسال به تلگرام: ${error.message}`, 'error');
      // خطای تلگرام نباید باعث توقف فرآیند اصلی شود
      return false;
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // Hook به سیستم فرم فعلی
  // ═══════════════════════════════════════════════════════════════

  /**
   * اتصال به سیستم فرم موجود سایت
   */
  function hookToExistingForm() {
    // روش ۱: اگر تابع submitLead وجود دارد
    if (window.submitLead && typeof window.submitLead === 'function') {
      log('تابع submitLead پیدا شد، در حال اتصال...', 'info');
      
      const originalSubmitLead = window.submitLead;
      
      window.submitLead = async function(leadData) {
        log('فرم در حال ارسال...', 'info');
        
        try {
          // ارسال به Google Sheets (روش اصلی)
          const result = await originalSubmitLead(leadData);
          
          // ارسال همزمان به تلگرام (بدون انتظار)
          sendToTelegram(leadData).catch(err => {
            log(`هشدار: ${err.message}`, 'warn');
          });
          
          return result;
          
        } catch (error) {
          log(`خطا در ارسال فرم: ${error.message}`, 'error');
          throw error;
        }
      };
      
      log('✅ اتصال تلگرام فعال شد', 'success');
      return true;
    }
    
    // روش ۲: رصد فرم‌های HTML
    log('تابع submitLead یافت نشد، رصد فرم‌های HTML...', 'warn');
    
    const forms = document.querySelectorAll('form');
    log(`${forms.length} فرم پیدا شد`, 'info');
    
    forms.forEach((form, index) => {
      form.addEventListener('submit', async function(e) {
        log(`فرم ${index + 1} ارسال شد`, 'info');
        
        // جمع‌آوری داده‌های فرم
        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());
        
        // اضافه کردن اطلاعات تکمیلی
        data.id = data.id || `lead-${Date.now()}`;
        data.createdAtFa = getPersianDateTime();
        data.pageUrl = window.location.href;
        
        // ارسال به تلگرام (بدون مسدود کردن فرم)
        sendToTelegram(data).catch(err => {
          log(`هشدار: ${err.message}`, 'warn');
        });
      });
    });
    
    return forms.length > 0;
  }

  // ═══════════════════════════════════════════════════════════════
  // راه‌اندازی اولیه
  // ═══════════════════════════════════════════════════════════════

  /**
   * شروع اتصال
   */
  function initialize() {
    log('شروع راه‌اندازی اتصال تلگرام...', 'info');
    
    // صبر تا DOM کامل بارگذاری شود
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', function() {
        setTimeout(hookToExistingForm, 500);
      });
    } else {
      // DOM از قبل آماده است
      setTimeout(hookToExistingForm, 500);
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // API عمومی
  // ═══════════════════════════════════════════════════════════════

  // در دسترس قرار دادن توابع برای استفاده خارجی
  window.NourotourTelegram = {
    send: sendToTelegram,
    config: CONFIG,
    version: '1.0.0'
  };

  // شروع خودکار
  initialize();

  log('ماژول تلگرام بارگذاری شد', 'success');

})();
