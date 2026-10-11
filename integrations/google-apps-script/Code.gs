/**
 * نوروتور — رلهٔ سروری درخواست‌های فرم مشاوره و رزرو (Google Apps Script → تلگرام)
 * نسخهٔ ۳
 *
 * چرا «رله»؟
 *   قبلاً پیام تلگرام از داخل مرورگر بازدیدکننده به api.telegram.org فرستاده می‌شد؛
 *   اما api.telegram.org از ایران فیلتر است، پس فرم «ثبت» می‌شد ولی هیچ پیامی نمی‌رسید.
 *   حالا مسیر ارسال این است:
 *       فرم سایت  →  همین Web app گوگل (از ایران باز می‌شود)  →  سرور گوگل  →  تلگرام
 *   هیچ داده‌ای در Google Sheets ثبت نمی‌شود؛ این اسکریپت فقط یک رلهٔ پیام است.
 *
 * راه‌اندازی / به‌روزرسانی (می‌توانید از گوشی هم انجام دهید):
 *   1) کد این فایل را کامل کپی کنید و در پروژهٔ Apps Script خود (script.google.com)
 *      کل کد قدیمی را پاک کنید و این را بچسبانید، بعد «ذخیره» (Ctrl+S).
 *   2) Deploy → Manage deployments → ✎ (Edit) → Version: «New version» → Deploy
 *      و اگر پنجرهٔ دسترسی (Authorize) آمد، تأیید کنید.
 *   3) یک‌بار تابع `testPost` را از منوی بالا انتخاب کنید و ▶ Run بزنید؛
 *      باید یک پیام آزمایشی در گروه تلگرام برسد.
 *
 * نکته‌ها:
 *   - نشانی /exec تغییری نمی‌کند؛ همان نشانی‌ای است که در index.html
 *     (defaultData.general.leadEndpoint) قرار دارد.
 *   - زمان اجرا: هر تغییر کد باید با «New version» منتشر شود، وگرنه /exec
 *     همچنان نسخهٔ قدیمی را اجرا می‌کند.
 *   - مرورگر به Apps Script نمی‌تواند درخواست با Content-Type: application/json بفرستد
 *     (بدون پاسخ به preflight، خطای CORS می‌دهد). پس سایت بدنهٔ JSON را با
 *     Content-Type: text/plain می‌فرستد و این اسکریپت آن را از e.postData.contents می‌خواند.
 *
 * تست دستی از ترمینال:
 *   curl -L -X POST "<نشانی exec>" -H "Content-Type: text/plain" \
 *        -d '{"kind":"consult","fullName":"تست","phone":"09120000000","destination":"استانبول"}'
 *   پاسخ موفق: {"status":"ok","telegram":true,"messageId":123}
 */

var CONFIG = {
  // توکن ربات تلگرام (BotFather). توجه: این توکن در مخزن عمومی است؛ پس از تأیید کارکرد،
  // بهتر است با /revoke در BotFather توکن جدید بگیرید و همین‌جا جای‌گزین کنید.
  BOT_TOKEN: "8898159650:AAEqqUDMSx3BOwzITqvq-a-mrXLI-tWIhNE",
  // شناسهٔ گروه/کانال مقصد (گروه: منفی؛ مثال: -1004463990161).
  CHAT_ID: "-1004463990161",
  // منطقهٔ زمانی برای «زمان دریافت» در نبود زمان شمسی ارسالی سایت.
  TIMEZONE: "Asia/Tehran",
  // حداکثر زمان انتظار برای پاسخ تلگرام (میلی‌ثانیه).
  TIMEOUT_MS: 10000,
  // حداکثر طول متن پیام (سقف تلگرام ۴۰۹۶ کاراکتر است).
  MAX_TEXT_LENGTH: 3800,
  // اگر روزی خواستید رله فقط برای نوع خاصی کار کند، مثلاً "consult,booking".
  // خالی = همهٔ انواع (به‌جز تلهٔ ربات‌ها).
  ALLOWED_KINDS: "consult,booking,test"
};

var KIND_LABELS = {
  consult: "🔔 درخواست مشاوره",
  booking: "✈️ درخواست رزرو",
  test: "🧪 ارسال آزمایشی"
};

/** بررسی سلامت: باز کردن نشانی /exec در مرورگر باید {"status":"ok"} برگرداند. */
function doGet() {
  return jsonResponse_({
    status: "ok",
    service: "nourotour-telegram-relay",
    version: 3,
    telegram: Boolean(CONFIG.BOT_TOKEN && CONFIG.CHAT_ID)
  });
}

/**
 * دریافت یک درخواست از سایت و «forward» آن به گروه تلگرام.
 * خروجی: {"status":"ok","telegram":true,"messageId":…,"kind":"consult"}
 *        یا {"status":"error","message":"…"}
 */
function doPost(e) {
  try {
    var lead = parseLead_(e);

    if (!lead || typeof lead !== "object" || Object.keys(lead).length === 0) {
      return jsonResponse_({ status: "error", message: "empty payload" });
    }

    var kind = String(lead.kind || "consult").trim() || "consult";

    if (!isKindAllowed_(kind)) {
      return jsonResponse_({ status: "ok", telegram: false, skipped: true, kind: kind });
    }

    var text = formatMessage_(lead, kind);
    var sent = sendToTelegram_(text, false);

    // اگر HTML معتبر نبود (مثلاً کاراکتر غیرمنتظره)، یک بار بدون قالب‌بندی دوباره امتحان می‌کنیم.
    if (!sent.ok && sent.retryAsPlainText) {
      sent = sendToTelegram_(stripHtml_(text), true);
    }

    if (!sent.ok) {
      console.error("telegram error: " + sent.description);
      return jsonResponse_({ status: "error", message: sent.description || "telegram send failed" });
    }

    return jsonResponse_({
      status: "ok",
      telegram: true,
      messageId: sent.messageId,
      kind: kind,
      kindLabel: KIND_LABELS[kind] || kind
    });
  } catch (err) {
    console.error(err);
    return jsonResponse_({ status: "error", message: String((err && err.message) || err) });
  }
}

/**
 * بدنهٔ درخواست را به شیء تبدیل می‌کند:
 *   - JSON خام (Content-Type: text/plain یا application/json) ← e.postData.contents
 *   - فرم معمولی (x-www-form-urlencoded / multipart) ← e.parameter (یا فیلد payload حاوی JSON)
 */
function parseLead_(e) {
  var contents = e && e.postData && e.postData.contents;

  if (contents) {
    try {
      var parsed = JSON.parse(contents);
      if (parsed && typeof parsed === "object") return parsed;
    } catch (ignored) {
      // JSON نبود؛ با پارامترهای فرم ادامه می‌دهیم.
    }
  }

  var params = (e && e.parameter) || {};

  if (params.payload) {
    try { return JSON.parse(params.payload); } catch (ignored) {}
  }

  var copy = {};
  Object.keys(params).forEach(function (key) { copy[key] = params[key]; });
  return copy;
}

function isKindAllowed_(kind) {
  var allowed = String(CONFIG.ALLOWED_KINDS || "").trim();
  if (!allowed) return true;

  return allowed.split(",").some(function (item) {
    return item.trim() === kind;
  });
}

/** ساخت متن HTML پیام تلگرام از رکورد دریافتی سایت. */
function formatMessage_(lead, kind) {
  var title = KIND_LABELS[kind] || "📋 درخواست جدید";
  var parts = ["<b>" + title + "</b>", "━━━━━━━━━━━━━━━━━━━", ""];

  parts.push("👤 <b>نام:</b> " + safe_(lead.fullName));
  parts.push("📱 <b>تلفن:</b> <code>" + safe_(lead.phone) + "</code>");

  if (lead.tour) parts.push("🎯 <b>تور:</b> " + safe_(lead.tour));
  if (lead.destination) parts.push("📍 <b>مقصد:</b> " + safe_(lead.destination));
  if (lead.travelers) parts.push("👥 <b>تعداد مسافر:</b> " + safe_(lead.travelers));
  if (lead.preferredDate) parts.push("📅 <b>تاریخ:</b> " + safe_(lead.preferredDate));
  if (lead.contactMethod) parts.push("📞 <b>روش تماس:</b> " + safe_(lead.contactMethod));
  if (lead.budget) parts.push("💰 <b>بودجه:</b> " + safe_(lead.budget));
  if (lead.message) parts.push("", "💬 <b>توضیحات:</b>", safe_(lead.message));

  parts.push("", "━━━━━━━━━━━━━━━━━━━");
  parts.push("🕐 <b>زمان:</b> " + safe_(lead.createdAtFa || nowInTehran_()));
  if (lead.id) parts.push("🆔 <b>شناسه:</b> <code>" + safe_(lead.id) + "</code>");
  if (lead.pageUrl) parts.push("🌐 <b>صفحه:</b> " + safe_(lead.pageUrl));
  parts.push("", "🌐 <a href=\"https://nourotour.ir\">nourotour.ir</a>");

  var text = parts.join("\n");

  if (text.length > CONFIG.MAX_TEXT_LENGTH) {
    text = text.slice(0, CONFIG.MAX_TEXT_LENGTH - 1) + "…";
  }

  return text;
}

/**
 * ارسال پیام به تلگرام از سمت سرور گوگل (تنها نقطهٔ تماس با api.telegram.org).
 * خروجی: { ok, messageId, description, retryAsPlainText }
 */
function sendToTelegram_(text, plainText) {
  var token = String(CONFIG.BOT_TOKEN || "").trim();
  var chatId = String(CONFIG.CHAT_ID || "").trim();

  if (!token || !chatId) {
    return { ok: false, description: "BOT_TOKEN یا CHAT_ID در تنظیمات خالی است" };
  }

  var payload = {
    chat_id: chatId,
    text: text,
    disable_web_page_preview: true
  };

  if (!plainText) payload.parse_mode = "HTML";

  var response;

  try {
    response = UrlFetchApp.fetch(
      "https://api.telegram.org/bot" + token + "/sendMessage",
      {
        method: "post",
        contentType: "application/json",
        payload: JSON.stringify(payload),
        muteHttpExceptions: true,
        followRedirects: true
      }
    );
  } catch (err) {
    return { ok: false, description: "خطای شبکه در تماس با تلگرام: " + ((err && err.message) || err) };
  }

  var body;
  try {
    body = JSON.parse(response.getContentText() || "{}");
  } catch (ignored) {
    body = null;
  }

  if (body && body.ok) {
    return { ok: true, messageId: body.result && body.result.message_id };
  }

  var description = (body && body.description) || ("HTTP " + response.getResponseCode());

  return {
    ok: false,
    description: description,
    // «Bad Request: can't parse entities…» یعنی متن HTML مشکل داشته؛ بدون parse_mode بفرست.
    retryAsPlainText: !plainText && /parse entities|can't parse|unsupported start tag/i.test(description)
  };
}

/** متن ساده (بدون تگ HTML) برای تلاش دوم در صورت خطای قالب‌بندی. */
function stripHtml_(text) {
  return String(text)
    .replace(/<br\s*\/?>/gi, "\n")
    .replace(/<[^>]+>/g, "")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, "\"")
    .replace(/&#039;/g, "'")
    .replace(/&amp;/g, "&");
}

function safe_(value) {
  if (value === undefined || value === null) return "—";
  var text = String(value).trim();
  if (!text) return "—";
  return escapeHtml_(text);
}

function escapeHtml_(text) {
  return String(text).replace(/[&<>"']/g, function (char) {
    if (char === "&") return "&amp;";
    if (char === "<") return "&lt;";
    if (char === ">") return "&gt;";
    if (char === "\"") return "&quot;";
    return "&#039;";
  });
}

function nowInTehran_() {
  return Utilities.formatDate(new Date(), CONFIG.TIMEZONE, "yyyy-MM-dd HH:mm");
}

function jsonResponse_(payload) {
  return ContentService
    .createTextOutput(JSON.stringify(payload))
    .setMimeType(ContentService.MimeType.JSON);
}

/**
 * اجرای آزمایشی از داخل ویرایشگر Apps Script (Run ▶ testPost).
 * بار اول پنجرهٔ تأیید دسترسی باز می‌شود؛ پس از تأیید باید یک پیام آزمایشی
 * در گروه تلگرام برسد و خروجی در Execution log دیده شود.
 */
function testPost() {
  var sample = {
    id: "test-" + Date.now(),
    kind: "test",
    createdAt: new Date().toISOString(),
    createdAtFa: Utilities.formatDate(new Date(), CONFIG.TIMEZONE, "yyyy/MM/dd HH:mm"),
    fullName: "اجرای آزمایشی از ویرایشگر",
    phone: "09120000000",
    destination: "استانبول",
    message: "اگر این پیام را در گروه تلگرام می‌بینید، رلهٔ سروری درست کار می‌کند.",
    pageUrl: "https://nourotour.ir/"
  };

  var output = doPost({ postData: { type: "text/plain", contents: JSON.stringify(sample) } });
  Logger.log(output.getContent());
}
