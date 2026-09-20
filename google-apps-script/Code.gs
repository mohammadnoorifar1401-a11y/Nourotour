/**
 * Nourotour - Google Apps Script Web App
 * این کد را به طور کامل در https://script.google.com جایگزین کد فعلی کنید
 * 
 * خطای "ReferenceError: https is not defined" به این دلیل است که
 * شما کد Node.js را در محیط Google Apps Script قرار داده‌اید.
 * در Apps Script ماژول https وجود ندارد. باید از سرویس‌های خود گوگل استفاده کنید.
 */

// تنظیمات شیت - اگر ID خاصی دارید اینجا بگذارید، در غیر اینصورت از شیت فعال استفاده می‌شود
// const SPREADSHEET_ID = "YOUR_SPREADSHEET_ID_HERE";
const SHEET_NAME = "Leads"; // نام برگه، اگر وجود نداشته باشد ساخته می‌شود

function getSheet() {
  var ss;
  // اگر SPREADSHEET_ID تعریف شده باشد، از آن استفاده کن
  // در غیر اینصورت از Spreadsheet متصل به پروژه استفاده کن
  try {
    if (typeof SPREADSHEET_ID !== 'undefined' && SPREADSHEET_ID) {
      ss = SpreadsheetApp.openById(SPREADSHEET_ID);
    } else {
      ss = SpreadsheetApp.getActiveSpreadsheet();
      if (!ss) {
        // اگر پروژه به شیت متصل نیست، یکی بساز یا خطا بده
        throw new Error("Spreadsheet متصل نیست. لطفا از منوی Extensions > Apps Script وارد شوید یا SPREADSHEET_ID را تنظیم کنید");
      }
    }
  } catch (e) {
    // fallback: سعی کن active را بگیری
    ss = SpreadsheetApp.getActiveSpreadsheet();
  }

  var sheet = ss.getSheetByName(SHEET_NAME);
  if (!sheet) {
    // اگر شیت Leads وجود نداشت، از اولین شیت استفاده کن یا بساز
    sheet = ss.getSheets()[0];
    if (!sheet) {
      sheet = ss.insertSheet(SHEET_NAME);
    }
  }

  // اگر شیت خالی است، هدر اضافه کن
  if (sheet.getLastRow() === 0) {
    sheet.appendRow([
      "id",
      "kind",
      "createdAt",
      "fullName",
      "phone",
      "tour",
      "destination",
      "travelers",
      "preferredDate",
      "contactMethod",
      "budget",
      "message",
      "pageUrl",
      "userAgent"
    ]);
    // استایل هدر
    var headerRange = sheet.getRange(1, 1, 1, 14);
    headerRange.setFontWeight("bold");
    headerRange.setBackground("#f4da91");
  }

  return sheet;
}

function doPost(e) {
  try {
    var sheet = getSheet();
    
    var payload = {};
    
    // روش 1: JSON در postData.contents (ارسال از سایت نوروتور)
    if (e.postData && e.postData.contents) {
      try {
        payload = JSON.parse(e.postData.contents);
      } catch (parseError) {
        // اگر JSON نبود، سعی کن پارامترها را بخوانی
        Logger.log("JSON parse failed, trying parameters: " + parseError);
        payload = e.parameter || {};
      }
    } else if (e.parameter) {
      // روش 2: فرم معمولی
      payload = e.parameter;
    }

    // لاگ برای دیباگ
    Logger.log("Received payload: " + JSON.stringify(payload));

    // اعتبارسنجی ساده
    if (!payload.fullName && !payload.phone && !payload.tour) {
      // ممکن است خالی باشد، اما باز هم ثبت می‌کنیم
      Logger.log("Warning: payload seems empty");
    }

    // افزودن ردیف جدید
    sheet.appendRow([
      payload.id || Utilities.getUuid(),
      payload.kind || "",
      payload.createdAt || new Date().toISOString(),
      payload.fullName || "",
      payload.phone || "",
      payload.tour || "",
      payload.destination || "",
      payload.travelers || "",
      payload.preferredDate || "",
      payload.contactMethod || "",
      payload.budget || "",
      payload.message || "",
      payload.pageUrl || "",
      payload.userAgent || (e.parameter && e.parameter.userAgent) || ""
    ]);

    // پاسخ موفق - مهم: باید JSON برگردانیم
    var response = {
      result: "success",
      message: "Lead saved successfully",
      id: payload.id
    };

    return ContentService
      .createTextOutput(JSON.stringify(response))
      .setMimeType(ContentService.MimeType.JSON);

  } catch (err) {
    Logger.log("Error in doPost: " + err.toString() + " Stack: " + err.stack);
    
    var errorResponse = {
      result: "error",
      message: err.toString()
    };

    return ContentService
      .createTextOutput(JSON.stringify(errorResponse))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

function doGet(e) {
  // برای تست اینکه وب اپ فعال است
  // لینک را در مرورگر باز کنید باید {"status":"ok"} ببینید
  
  // اگر پارامتری فرستاده شده، آن را هم به عنوان POST در نظر بگیر (برای تست)
  if (e.parameter && (e.parameter.fullName || e.parameter.phone)) {
    return doPost(e);
  }

  var response = {
    status: "ok",
    message: "Nourotour webhook is active",
    time: new Date().toISOString(),
    info: "برای اتصال فرم‌های سایت، این URL را در پنل مدیریت (Ctrl+Shift+A) وارد کنید"
  };

  return ContentService
    .createTextOutput(JSON.stringify(response))
    .setMimeType(ContentService.MimeType.JSON);
}

// تابع تست دستی - از داخل Apps Script اجرا کنید
function testSave() {
  var mockEvent = {
    postData: {
      contents: JSON.stringify({
        id: "test-" + new Date().getTime(),
        kind: "consult",
        createdAt: new Date().toISOString(),
        fullName: "تست نوروتور",
        phone: "09120000000",
        destination: "استانبول",
        budget: "تا ۳۰ میلیون",
        message: "این یک تست است",
        pageUrl: "https://nourotour.ir"
      })
    }
  };
  
  var result = doPost(mockEvent);
  Logger.log(result.getContent());
}
