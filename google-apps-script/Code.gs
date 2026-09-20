/**
 * Nourotour - نسخه ساده و تمیز
 * این تنها کدی است که باید در Google Apps Script باشد
 */

function doPost(e) {
  try {
    // شیت فعال را بگیر (چون اسکریپت را از داخل شیت می‌سازی، فعال وجود دارد)
    var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
    
    // اگر شیت خالی بود، هدر بساز
    if (sheet.getLastRow() === 0) {
      sheet.appendRow([
        "تاریخ", "نوع درخواست", "نام", "موبایل", "تور", "مقصد", "تعداد", "تاریخ سفر", "بودجه", "پیام", "لینک صفحه"
      ]);
    }

    // داده‌ای که از سایت آمده را بخوان
    var data = {};
    if (e.postData && e.postData.contents) {
      data = JSON.parse(e.postData.contents);
    } else {
      data = e.parameter;
    }

    // یک ردیف جدید اضافه کن
    sheet.appendRow([
      new Date(), // تاریخ شمسی/میلادی خودکار
      data.kind || "",
      data.fullName || "",
      data.phone || "",
      data.tour || "",
      data.destination || "",
      data.travelers || "",
      data.preferredDate || "",
      data.budget || "",
      data.message || "",
      data.pageUrl || ""
    ]);

    // جواب موفقیت به سایت
    return ContentService
      .createTextOutput(JSON.stringify({result: "success"}))
      .setMimeType(ContentService.MimeType.JSON);

  } catch (err) {
    // اگر خطا خورد، خطا را برگردان تا بفهمیم
    return ContentService
      .createTextOutput(JSON.stringify({result: "error", error: err.toString()}))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

function doGet(e) {
  // وقتی لینک را در مرورگر باز می‌کنی این را می‌بینی
  return ContentService
    .createTextOutput(JSON.stringify({status: "ok", message: "نوروتور فعال است - Nourotour is running"}))
    .setMimeType(ContentService.MimeType.JSON);
}
