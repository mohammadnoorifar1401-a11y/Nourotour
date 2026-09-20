/**
 * نوروتور — گیرندهٔ درخواست‌های فرم مشاوره و رزرو (Google Apps Script → Google Sheets)
 * نسخهٔ ۲
 *
 * این فایل را به‌جای محتوای Code.gs در پروژهٔ Apps Script بگذارید و «ذخیره» کنید.
 *
 * راه‌اندازی (یک‌بار):
 *   1) در Google Sheets یک فایل بسازید (مثلاً «Nourotour Leads») → Extensions → Apps Script.
 *   2) این کد را جای‌گزین کنید و ذخیره کنید. یک‌بار تابع `testPost` را از منوی بالا اجرا کنید
 *      تا دسترسی‌ها (Sheets و در صورت نیاز Gmail) تأیید شوند و یک ردیف آزمایشی ثبت شود.
 *   3) Deploy → New deployment → نوع: Web app
 *        Description: nourotour leads
 *        Execute as:      Me (ایمیل خودتان)
 *        Who has access:  Anyone   ← حتماً «Anyone» و نه «Anyone with Google account»
 *   4) نشانی /exec را کپی کنید؛ همین نشانی در index.html (defaultData.general.leadEndpoint) قرار دارد.
 *
 * ⚠️ بعد از هر تغییر در این کد: Deploy → Manage deployments → ✎ (Edit) → Version: «New version» → Deploy
 *    وگرنه نشانی /exec همچنان نسخهٔ قدیمی را اجرا می‌کند.
 *
 * نکتهٔ CORS: مرورگر به Apps Script نمی‌تواند درخواست با Content-Type: application/json بفرستد
 * (preflight پشتیبانی نمی‌شود). سایت، بدنهٔ JSON را با Content-Type: text/plain می‌فرستد و این
 * اسکریپت آن را از `e.postData.contents` می‌خواند. پارامترهای فرم معمولی (x-www-form-urlencoded)
 * هم پشتیبانی می‌شود.
 *
 * تست دستی از ترمینال:
 *   curl -L -X POST "<نشانی exec>" -H "Content-Type: text/plain" \
 *        -d '{"kind":"consult","fullName":"تست","phone":"09120000000","destination":"استانبول"}'
 *   پاسخ موفق: {"status":"ok","id":"…","row":N}
 */

var CONFIG = {
  // خالی = اسپردشیتی که این اسکریپت به آن متصل است (Extensions → Apps Script).
  // اگر اسکریپت مستقل است، شناسهٔ فایل Sheets را از نشانی آن این‌جا بگذارید.
  SHEET_ID: "",
  // نام برگه‌ای که رکوردها در آن ذخیره می‌شوند (در صورت نبود، ساخته می‌شود).
  SHEET_NAME: "Leads",
  // برای دریافت ایمیل با هر درخواست، نشانی را بنویسید (مثلاً "info@nourotour.ir"). خالی = بدون ایمیل.
  NOTIFY_EMAIL: "",
  // منطقهٔ زمانی ستون «زمان دریافت».
  TIMEZONE: "Asia/Tehran"
};

// ستون‌های برگه به ترتیب: [کلید در JSON ارسالی سایت، عنوان ستون]
var COLUMNS = [
  ["receivedAt", "زمان دریافت (تهران)"],
  ["createdAtFa", "زمان ثبت (شمسی)"],
  ["kindLabel", "نوع درخواست"],
  ["fullName", "نام و نام خانوادگی"],
  ["phone", "شماره تماس"],
  ["tour", "تور"],
  ["destination", "مقصد"],
  ["travelers", "تعداد مسافر"],
  ["preferredDate", "تاریخ پیشنهادی"],
  ["contactMethod", "روش تماس"],
  ["budget", "بودجه تقریبی"],
  ["message", "توضیحات"],
  ["id", "شناسه"],
  ["kind", "کد نوع"],
  ["createdAt", "زمان ثبت (ISO)"],
  ["pageUrl", "صفحهٔ ارسال"],
  ["extra", "سایر فیلدها (JSON)"]
];

var KIND_LABELS = {
  consult: "درخواست مشاوره",
  booking: "درخواست رزرو",
  test: "ارسال آزمایشی"
};

/** بررسی سلامت: باز کردن نشانی /exec در مرورگر باید {"status":"ok"} برگرداند. */
function doGet() {
  return jsonResponse_({ status: "ok", service: "nourotour-leads", version: 2 });
}

/** دریافت یک درخواست از سایت و افزودن یک ردیف به Google Sheets. */
function doPost(e) {
  var lock = LockService.getScriptLock();

  try {
    lock.waitLock(20000);

    var lead = parseLead_(e);

    if (!lead || typeof lead !== "object" || Object.keys(lead).length === 0) {
      return jsonResponse_({ status: "error", message: "empty payload" });
    }

    lead.kindLabel = KIND_LABELS[lead.kind] || String(lead.kind || "");
    lead.receivedAt = Utilities.formatDate(new Date(), CONFIG.TIMEZONE, "yyyy-MM-dd HH:mm:ss");

    var known = {};
    COLUMNS.forEach(function (column) { known[column[0]] = true; });

    var extra = {};
    Object.keys(lead).forEach(function (key) {
      if (!known[key] && key !== "site" && key !== "syncState") extra[key] = lead[key];
    });
    lead.extra = Object.keys(extra).length ? JSON.stringify(extra) : "";

    var sheet = getSheet_();
    var row = COLUMNS.map(function (column) {
      var value = lead[column[0]];
      return value === undefined || value === null ? "" : String(value);
    });

    sheet.appendRow(row);
    var rowNumber = sheet.getLastRow();

    notify_(lead);

    return jsonResponse_({ status: "ok", id: String(lead.id || ""), row: rowNumber });
  } catch (err) {
    console.error(err);
    return jsonResponse_({ status: "error", message: String((err && err.message) || err) });
  } finally {
    try { lock.releaseLock(); } catch (ignored) {}
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
      // JSON نبود؛ با پارامترها ادامه می‌دهیم.
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

/** برگهٔ مقصد را برمی‌گرداند و در صورت نیاز آن را با ردیف عنوان می‌سازد. */
function getSheet_() {
  var spreadsheet = null;

  if (CONFIG.SHEET_ID) {
    spreadsheet = SpreadsheetApp.openById(CONFIG.SHEET_ID);
  } else {
    spreadsheet = SpreadsheetApp.getActiveSpreadsheet();
  }

  if (!spreadsheet) {
    // اسکریپت مستقل بدون SHEET_ID: یک‌بار فایل می‌سازیم و شناسه‌اش را نگه می‌داریم.
    var props = PropertiesService.getScriptProperties();
    var savedId = props.getProperty("SHEET_ID");

    if (savedId) {
      spreadsheet = SpreadsheetApp.openById(savedId);
    } else {
      spreadsheet = SpreadsheetApp.create("Nourotour Leads");
      props.setProperty("SHEET_ID", spreadsheet.getId());
    }
  }

  var sheet = spreadsheet.getSheetByName(CONFIG.SHEET_NAME);
  if (!sheet) sheet = spreadsheet.insertSheet(CONFIG.SHEET_NAME);

  if (sheet.getLastRow() === 0) {
    sheet.appendRow(COLUMNS.map(function (column) { return column[1]; }));
    sheet.setFrozenRows(1);
    sheet.setRightToLeft(true);
    sheet.getRange(1, 1, 1, COLUMNS.length).setFontWeight("bold");
    // همهٔ ستون‌ها متنی باشند تا صفرِ ابتدای شماره‌های موبایل (0912…) حذف نشود.
    sheet.getRange(1, 1, sheet.getMaxRows(), COLUMNS.length).setNumberFormat("@");
  }

  return sheet;
}

/** اعلان ایمیلی اختیاری برای هر درخواست. */
function notify_(lead) {
  if (!CONFIG.NOTIFY_EMAIL) return;

  try {
    var lines = COLUMNS
      .filter(function (column) {
        return column[0] !== "extra" && column[0] !== "kind" && lead[column[0]];
      })
      .map(function (column) { return column[1] + ": " + lead[column[0]]; });

    MailApp.sendEmail({
      to: CONFIG.NOTIFY_EMAIL,
      subject: "نوروتور — " + (lead.kindLabel || "درخواست جدید") + (lead.fullName ? " — " + lead.fullName : ""),
      body: lines.join("\n")
    });
  } catch (err) {
    console.warn("email notification failed: " + err);
  }
}

function jsonResponse_(payload) {
  return ContentService
    .createTextOutput(JSON.stringify(payload))
    .setMimeType(ContentService.MimeType.JSON);
}

/**
 * اجرای آزمایشی از داخل ویرایشگر Apps Script (Run ▶ testPost).
 * بار اول پنجرهٔ تأیید دسترسی باز می‌شود؛ پس از تأیید، یک ردیف آزمایشی در برگه ثبت می‌شود
 * و خروجی در Execution log دیده می‌شود.
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
    message: "اگر این ردیف را می‌بینید، اسکریپت به Google Sheets دسترسی دارد.",
    pageUrl: "https://nourotour.ir/"
  };

  var output = doPost({ postData: { type: "text/plain", contents: JSON.stringify(sample) } });
  Logger.log(output.getContent());
}
