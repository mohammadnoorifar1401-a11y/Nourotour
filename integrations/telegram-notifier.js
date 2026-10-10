/**
 * نوروتور — ارسال اطلاع‌رسانی فرم‌ها به تلگرام
 * 
 * این فایل را می‌توانید مستقیماً در index.html قرار دهید یا به‌عنوان یک فایل جداگانه لود کنید.
 * وقتی کاربر فرم درخواست سفر/مشاوره را ارسال می‌کند، اطلاعات به تلگرام هم ارسال می‌شود.
 */

(function() {
  'use strict';

  // تنظیمات تلگرام
  const TELEGRAM_CONFIG = {
    botToken: '8898159650:AAEqqUDMSx3BOwzITqvq-a-mrXLI-tWIhNE',
    chatId: '-1004463990161'
  };

  /**
   * ارسال پیام به گروه تلگرام
   * @param {Object} leadData - اطلاعات فرم ارسال شده
   * @returns {Promise<boolean>} - موفقیت ارسال
   */
  async function sendToTelegram(leadData) {
    try {
      const message = formatLeadMessage(leadData);
      const url = `https://api.telegram.org/bot${TELEGRAM_CONFIG.botToken}/sendMessage`;

      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          chat_id: TELEGRAM_CONFIG.chatId,
          text: message,
          parse_mode: 'HTML',
          disable_web_page_preview: true
        })
      });

      const result = await response.json();
      
      if (!result.ok) {
        console.error('خطا در ارسال به تلگرام:', result);
        return false;
      }

      console.log('✅ پیام با موفقیت به تلگرام ارسال شد');
      return true;
    } catch (error) {
      console.error('خطا در ارسال به تلگرام:', error);
      return false;
    }
  }

  /**
   * فرمت کردن اطلاعات فرم برای نمایش در تلگرام
   * @param {Object} lead - اطلاعات فرم
   * @returns {string} - پیام فرمت شده
   */
  function formatLeadMessage(lead) {
    const kindLabels = {
      consult: '🔔 درخواست مشاوره',
      booking: '✈️ درخواست رزرو',
      test: '🧪 ارسال آزمایشی'
    };

    const kindEmoji = {
      consult: '💬',
      booking: '🎫',
      test: '🧪'
    };

    const emoji = kindEmoji[lead.kind] || '📋';
    const title = kindLabels[lead.kind] || 'درخواست جدید';

    let message = `<b>${emoji} ${title}</b>\n`;
    message += `━━━━━━━━━━━━━━━━━━━\n\n`;

    // اطلاعات اصلی
    if (lead.fullName) {
      message += `👤 <b>نام:</b> ${escapeHtml(lead.fullName)}\n`;
    }

    if (lead.phone) {
      message += `📱 <b>تلفن:</b> <code>${escapeHtml(lead.phone)}</code>\n`;
    }

    // اطلاعات سفر
    if (lead.tour) {
      message += `🎯 <b>تور:</b> ${escapeHtml(lead.tour)}\n`;
    }

    if (lead.destination) {
      message += `📍 <b>مقصد:</b> ${escapeHtml(lead.destination)}\n`;
    }

    if (lead.travelers) {
      message += `👥 <b>تعداد مسافر:</b> ${escapeHtml(lead.travelers)}\n`;
    }

    if (lead.preferredDate) {
      message += `📅 <b>تاریخ پیشنهادی:</b> ${escapeHtml(lead.preferredDate)}\n`;
    }

    if (lead.budget) {
      message += `💰 <b>بودجه:</b> ${escapeHtml(lead.budget)}\n`;
    }

    if (lead.contactMethod) {
      message += `📞 <b>روش تماس:</b> ${escapeHtml(lead.contactMethod)}\n`;
    }

    // توضیحات
    if (lead.message) {
      message += `\n💬 <b>توضیحات:</b>\n${escapeHtml(lead.message)}\n`;
    }

    // اطلاعات تکمیلی
    message += `\n━━━━━━━━━━━━━━━━━━━\n`;
    
    if (lead.createdAtFa) {
      message += `🕐 <b>زمان:</b> ${escapeHtml(lead.createdAtFa)}\n`;
    }

    if (lead.id) {
      message += `🆔 <b>شناسه:</b> <code>${escapeHtml(lead.id)}</code>\n`;
    }

    // لینک به سایت
    message += `\n🌐 <a href="https://nourotour.ir">nourotour.ir</a>`;

    return message;
  }

  /**
   * تبدیل کاراکترهای HTML به متن امن
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
   * Hook به تابع submitLead موجود در سایت
   */
  function hookFormSubmission() {
    // اگر تابع submitLead از قبل وجود دارد، آن را wrap می‌کنیم
    if (window.submitLead && typeof window.submitLead === 'function') {
      const originalSubmitLead = window.submitLead;
      
      window.submitLead = async function(leadData) {
        // ابتدا فرم را به روش عادی ارسال می‌کنیم (Google Sheets)
        const result = await originalSubmitLead(leadData);
        
        // سپس به تلگرام هم ارسال می‌کنیم (در پس‌زمینه)
        // این کار نباید باعث توقف فرآیند اصلی شود
        sendToTelegram(leadData).catch(err => {
          console.warn('ارسال به تلگرام با خطا مواجه شد:', err);
        });
        
        return result;
      };
      
      console.log('✅ Telegram notifier متصل شد');
    } else {
      console.warn('⚠️ تابع submitLead یافت نشد. لطفاً مطمئن شوید که این اسکریپت بعد از اسکریپت اصلی سایت بارگذاری می‌شود.');
    }
  }

  // اجرای خودکار بعد از بارگذاری صفحه
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', hookFormSubmission);
  } else {
    hookFormSubmission();
  }

  // در دسترس قرار دادن تابع برای استفاده مستقیم
  window.sendToTelegram = sendToTelegram;

})();
