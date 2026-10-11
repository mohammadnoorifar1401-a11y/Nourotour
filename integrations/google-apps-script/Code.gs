/**
 * نوروتور — رلهٔ سروری درخواست‌های فرم مشاوره و رزرو (Google Apps Script → تلگرام)
 * نسخهٔ ۴
 *
 * مسیر ارسال:
 *   فرم سایت → همین Web app گوگل → سرور گوگل → تلگرام
 * این اسکریپت فقط پیام را رله می‌کند و هیچ داده‌ای در Google Sheets ثبت نمی‌شود.
 *
 * راه‌اندازی / به‌روزرسانی:
 *   1) کد این فایل را کامل در پروژهٔ Apps Script جای‌گزین و ذخیره کنید.
 *   2) Project Settings → Script properties: کلید BOT_TOKEN را بسازید و توکن تازه را وارد کنید.
 *      CHAT_ID اختیاری است؛ اگر تنظیم نشود، مقدار پیش‌فرض CONFIG.CHAT_ID استفاده می‌شود.
 *   3) تابع checkConfig را اجرا کنید؛ توکن و گروه بررسی می‌شوند، اما پیامی ارسال نمی‌شود.
 *   4) تابع testPost را اجرا کنید تا یک پیام آزمایشی در گروه بفرستد.
 *   5) Deploy → Manage deployments → ✎ → Version: New version → Deploy.
 *
 * نکته: اگر توکن قبلی در مخزن عمومی بوده است، آن را در @BotFather باطل کنید و توکن تازه بسازید.
 * پس از هر تغییر کد، /exec فقط با انتشار New version به‌روز می‌شود.
 */

var CONFIG = {
  // رازها را commit نکنید؛ توکن از Project Settings → Script properties خوانده می‌شود.
  // این مقدار فقط برای توسعهٔ محلی است و باید در مخزن خالی بماند.
  BOT_TOKEN: "",
  // شناسهٔ مقصد پیش‌فرض؛ با Script property به همین نام قابل بازنویسی است.
  CHAT_ID: "-1004463990161",
  TIMEZONE: "Asia/Tehran",
  TIMEOUT_MS: 10000,
  MAX_TEXT_LENGTH: 3800,
  ALLOWED_KINDS: "consult,booking,test"
};

var KIND_LABELS = {
  consult: "🔔 درخواست مشاوره",
  booking: "✈️ درخواست رزرو",
  test: "🧪 ارسال آزمایشی"
};

/** تنظیمات امن؛ توکن از Script properties می‌آید و هرگز در پاسخ عمومی برگردانده نمی‌شود. */
function getRelaySettings_() {
  var propertyToken = "";
  var propertyChatId = "";

  try {
    var properties = PropertiesService.getScriptProperties();
    propertyToken = properties.getProperty("BOT_TOKEN") || "";
    propertyChatId = properties.getProperty("CHAT_ID") || "";
  } catch (ignored) {
    // در محیط Apps Script این سرویس موجود است؛ مقدارهای CONFIG برای توسعهٔ محلی باقی می‌مانند.
  }

  var normalizedPropertyToken = normalizeTelegramToken_(propertyToken);
  var fallbackToken = normalizeTelegramToken_(CONFIG.BOT_TOKEN);

  return {
    token: normalizedPropertyToken || fallbackToken,
    chatId: String(propertyChatId || CONFIG.CHAT_ID || "").trim(),
    tokenFromProperties: Boolean(normalizedPropertyToken),
    chatIdFromProperties: Boolean(String(propertyChatId || "").trim())
  };
}

/** حذف فاصله‌ها و نویسه‌های نامرئی؛ پیشوند اختیاری bot هم پذیرفته نمی‌شود. */
function normalizeTelegramToken_(value) {
  return String(value || "")
    .replace(/[\s\u200b-\u200e\u202a-\u202e\ufeff]/g, "")
    .replace(/^bot/i, "");
}

function maskToken_(token) {
  var value = String(token || "");
  if (!value) return "تنظیم نشده";
  return "••••••" + value.slice(-4);
}

function tokenStatus_(settings) {
  if (!settings.token) return "missing (script-properties)";
  return settings.tokenFromProperties
    ? "configured (script-properties)"
    : "configured (CONFIG fallback)";
}

/** بررسی سلامت /exec؛ وضعیت تنظیمات را می‌دهد و هیچ راز یا توکنی را افشا نمی‌کند. */
function doGet() {
  var settings = getRelaySettings_();
  return jsonResponse_({
    status: "ok",
    service: "nourotour-telegram-relay",
    version: 4,
    token: tokenStatus_(settings),
    chat: settings.chatId
      ? (settings.chatIdFromProperties ? "configured (script-properties)" : "configured (default)")
      : "missing",
    telegram: Boolean(settings.token && settings.chatId)
  });
}

/**
 * دریافت درخواست فرم و ارسال آن به گروه تلگرام.
 * پاسخ خطا همیشه message و hint فارسی دارد تا علت در تب «درخواست‌ها» دیده شود.
 */
function doPost(e) {
  try {
    var lead = parseLead_(e);

    if (!lead || typeof lead !== "object" || Object.keys(lead).length === 0) {
      return jsonResponse_({
        status: "error",
        message: "بدنهٔ درخواست خالی است",
        hint: "داده‌های فرم را بررسی کنید و دوباره بفرستید."
      });
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
      return jsonResponse_({
        status: "error",
        message: sent.description || "ارسال پیام به تلگرام ناموفق بود",
        hint: sent.hint || hintForTelegramError_(sent.description)
      });
    }

    return jsonResponse_({
      status: "ok",
      telegram: true,
      messageId: sent.messageId,
      kind: kind,
      kindLabel: KIND_LABELS[kind] || kind
    });
  } catch (err) {
    var message = safeErrorMessage_(err);
    console.error("relay error: " + message);
    return jsonResponse_({
      status: "error",
      message: message,
      hint: hintForTelegramError_(message)
    });
  }
}

function safeErrorMessage_(err) {
  var message = String((err && err.message) || err || "خطای داخلی رله");
  try {
    var token = getRelaySettings_().token;
    if (token) message = message.split(token).join("[توکن پنهان شد]");
  } catch (ignored) {}
  return message.slice(0, 200);
}

function hintForTelegramError_(description) {
  var message = String(description || "");

  if (/unauthorized/i.test(message)) {
    return "توکن revoke شده؛ از @BotFather توکن تازه بگیر و در Script properties بگذار";
  }
  if (/chat not found/i.test(message)) {
    return "CHAT_ID اشتباه است یا ربات عضو گروه نیست؛ شناسهٔ مقصد و عضویت ربات را بررسی کن";
  }
  if (/CHAT_WRITE_FORBIDDEN|not enough rights|have no rights|not allowed to send|cannot send messages|can't send messages|bot was kicked|not a member|administrator rights/i.test(message)) {
    return "به ربات اجازهٔ ارسال پیام در گروه داده نشده؛ عضویت ربات و مجوز ارسال پیام را بررسی کن";
  }
  if (/BOT_TOKEN/i.test(message) && /تنظیم|خالی|missing|empty|not configured/i.test(message)) {
    return "کلید BOT_TOKEN را در Project Settings → Script properties تنظیم کن";
  }
  if (/CHAT_ID/i.test(message) && /تنظیم|خالی|missing|empty|not configured/i.test(message)) {
    return "کلید CHAT_ID را در Script properties بررسی کن یا مقدار پیش‌فرض را حفظ کن";
  }
  if (/network|fetch|timeout|شبکه/i.test(message)) {
    return "اتصال خروجی Apps Script به تلگرام را بررسی کن و دوباره تلاش کن";
  }

  return "تنظیمات BOT_TOKEN و CHAT_ID، عضویت ربات و مجوز ارسال پیام را بررسی کن";
}

/**
 * بدنهٔ درخواست را به شیء تبدیل می‌کند:
 *   - JSON خام (Content-Type: text/plain یا application/json) ← e.postData.contents
 *   - فرم معمولی (x-www-form-urlencoded / multipart) ← e.parameter یا فیلد payload
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

/** تماس سروری با Bot API؛ token از settings می‌آید و URL هیچ‌وقت در log چاپ نمی‌شود. */
function telegramApiRequest_(method, settings, requestMethod, payload) {
  var url = "https://api.telegram.org/bot" + settings.token + "/" + method;
  var httpMethod = String(requestMethod || "get").toLowerCase();
  var options = {
    method: httpMethod,
    muteHttpExceptions: true,
    followRedirects: true
  };

  if (httpMethod === "get" && payload) {
    var query = Object.keys(payload).map(function (key) {
      return encodeURIComponent(key) + "=" + encodeURIComponent(payload[key]);
    }).join("&");
    if (query) url += "?" + query;
  } else if (httpMethod === "post") {
    options.contentType = "application/json";
    options.payload = JSON.stringify(payload || {});
  }

  var response;
  try {
    response = UrlFetchApp.fetch(url, options);
  } catch (ignored) {
    var networkMessage = "خطای شبکه در تماس با تلگرام";
    return {
      ok: false,
      description: networkMessage,
      hint: hintForTelegramError_(networkMessage)
    };
  }

  var body;
  try {
    body = JSON.parse(response.getContentText() || "{}");
  } catch (ignored) {
    body = null;
  }

  if (body && body.ok) {
    return { ok: true, result: body.result || {}, responseCode: response.getResponseCode() };
  }

  var description = (body && body.description) || ("HTTP " + response.getResponseCode());
  return {
    ok: false,
    description: description,
    hint: hintForTelegramError_(description)
  };
}

/** ارسال پیام به تلگرام؛ خروجی شامل hint فارسی برای نمایش در پنل مدیریت است. */
function sendToTelegram_(text, plainText) {
  var settings = getRelaySettings_();

  if (!settings.token) {
    return {
      ok: false,
      description: "BOT_TOKEN تنظیم نشده است",
      hint: "کلید BOT_TOKEN را در Project Settings → Script properties تنظیم کن"
    };
  }
  if (!settings.chatId) {
    return {
      ok: false,
      description: "CHAT_ID تنظیم نشده است",
      hint: "کلید CHAT_ID را در Script properties بررسی کن یا مقدار پیش‌فرض را حفظ کن"
    };
  }

  var payload = {
    chat_id: settings.chatId,
    text: text,
    disable_web_page_preview: true
  };
  if (!plainText) payload.parse_mode = "HTML";

  var result = telegramApiRequest_("sendMessage", settings, "post", payload);
  if (result.ok) {
    return { ok: true, messageId: result.result.message_id };
  }

  var description = result.description || "ارسال پیام به تلگرام ناموفق بود";
  return {
    ok: false,
    description: description,
    hint: result.hint || hintForTelegramError_(description),
    // در صورت خطای HTML یک بار بدون parse_mode دوباره امتحان می‌کنیم.
    retryAsPlainText: !plainText && /parse entities|can't parse|unsupported start tag/i.test(description)
  };
}

/**
 * بررسی تنظیمات بدون ارسال پیام: ابتدا getMe برای توکن و سپس getChat برای مقصد.
 * نتیجهٔ فارسی در Execution log چاپ می‌شود و توکن فقط به شکل ماسک دیده می‌شود.
 */
function checkConfig() {
  var settings = getRelaySettings_();
  Logger.log("بررسی پیکربندی رلهٔ تلگرام نوروتور — نسخهٔ ۴");
  Logger.log("توکن: " + maskToken_(settings.token) + (settings.tokenFromProperties ? " (Script properties)" : ""));
  Logger.log("CHAT_ID: " + (settings.chatId || "تنظیم نشده"));

  if (!settings.token) {
    var missingTokenHint = "کلید BOT_TOKEN را در Project Settings → Script properties تنظیم کن";
    Logger.log("❌ BOT_TOKEN تنظیم نشده است");
    Logger.log("راهنما: " + missingTokenHint);
    return { status: "error", message: "BOT_TOKEN تنظیم نشده است", hint: missingTokenHint };
  }
  if (!settings.chatId) {
    var missingChatHint = "کلید CHAT_ID را در Script properties بررسی کن یا مقدار پیش‌فرض را حفظ کن";
    Logger.log("❌ CHAT_ID تنظیم نشده است");
    Logger.log("راهنما: " + missingChatHint);
    return { status: "error", message: "CHAT_ID تنظیم نشده است", hint: missingChatHint };
  }

  var me = telegramApiRequest_("getMe", settings, "get", null);
  if (!me.ok) {
    Logger.log("❌ اعتبارسنجی توکن ناموفق بود: " + me.description);
    Logger.log("راهنما: " + me.hint);
    return { status: "error", step: "getMe", message: me.description, hint: me.hint };
  }

  var username = me.result && me.result.username ? "@" + me.result.username : "ربات";
  Logger.log("✅ توکن معتبر است؛ ربات: " + username);

  var chat = telegramApiRequest_("getChat", settings, "get", { chat_id: settings.chatId });
  if (!chat.ok) {
    Logger.log("❌ دسترسی به گروه ناموفق بود: " + chat.description);
    Logger.log("راهنما: " + chat.hint);
    return { status: "error", step: "getChat", message: chat.description, hint: chat.hint };
  }

  var chatName = (chat.result && (chat.result.title || chat.result.username)) || settings.chatId;
  Logger.log("✅ گروه در دسترس است: " + chatName + " (" + settings.chatId + ")");
  Logger.log("✅ بررسی تمام شد؛ هیچ پیامی به گروه ارسال نشد.");
  return { status: "ok", telegram: true, bot: username, chat: chatName };
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
 * یک پیام آزمایشی می‌فرستد و نتیجه + hint را در Execution log چاپ می‌کند.
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

  var sent = sendToTelegram_(formatMessage_(sample, "test"), false);
  if (!sent.ok && sent.retryAsPlainText) {
    sent = sendToTelegram_(stripHtml_(formatMessage_(sample, "test")), true);
  }

  if (sent.ok) {
    var success = { status: "ok", telegram: true, messageId: sent.messageId };
    Logger.log("✅ testPost موفق بود؛ messageId=" + sent.messageId);
    return success;
  }

  var hint = sent.hint || hintForTelegramError_(sent.description);
  Logger.log("❌ testPost ناموفق بود: " + sent.description);
  Logger.log("راهنما: " + hint);
  return { status: "error", message: sent.description, hint: hint };
}
