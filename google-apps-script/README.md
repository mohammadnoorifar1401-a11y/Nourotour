# حل خطای `https is not defined` در Google Apps Script

## ❌ علت خطا
شما کدی شبیه این را در Apps Script قرار داده‌اید:

```javascript
const https = require('https'); // یا
https.request(...) // یا
const axios = require('axios')
```

**محیط Google Apps Script با Node.js فرق دارد!**
- ماژول `https` وجود ندارد
- `require` وجود ندارد  
- `axios`, `node-fetch` و ... کار نمی‌کنند
- فقط سرویس‌های داخلی گوگل مثل `SpreadsheetApp`, `ContentService`, `UrlFetchApp` در دسترس هستند

تصویری که فرستادید دقیقا همین را نشان می‌دهد: خط ۳ فایل Code شما `https` را نمی‌شناسد.

## ✅ راه حل

### مرحله ۱: جایگزینی کد
۱. به https://script.google.com بروید
۲. پروژه مربوط به لینک خود را باز کنید:
   `https://script.google.com/macros/s/AKfycbxWTcIbk2a3ot9XUaqke2L40JOL6Wc7uhpdNkyAQVPHy2I1aORhaeWICiip0tv0_Zhg/exec`
۳. تمام محتوای فایل `Code.gs` را پاک کنید
۴. محتوای فایل `Code.gs` همین پوشه (`google-apps-script/Code.gs`) را کپی و جایگزین کنید

### مرحله ۲: اتصال به Google Sheets
دو راه دارید:

**راه ساده (پیشنهادی):**
- در همان پروژه Apps Script، یک Google Sheet جدید بسازید از منوی بالا یا
- مطمئن شوید پروژه از داخل یک Google Sheet ساخته شده:
  Google Sheets > Extensions > Apps Script

**راه پیشرفته:**
- اگر شیت شما جای دیگری است، ID آن را در کد قرار دهید:
```javascript
const SPREADSHEET_ID = "1AbC...xyz";
```
ID را از URL شیت بردارید: `https://docs.google.com/spreadsheets/d/SPREADSHEET_ID/edit`

### مرحله ۳: Deploy صحیح
این مهم‌ترین بخش است:

1. در Apps Script دکمه **Deploy > New deployment** یا **Manage deployments > Edit** را بزنید
2. تنظیمات:
   - **Type:** Web app
   - **Description:** Nourotour v2
   - **Execute as:** Me
   - **Who has access:** **Anyone** (مهم! باید Anyone باشد تا سایت بتواند درخواست بفرستد)
3. **Deploy** کنید
4. URL جدید را کپی کنید (اگر تغییر کرد) و در سایت قرار دهید

### مرحله ۴: تست
- URL وب اپ را در مرورگر باز کنید، باید ببینید:
```json
{"status":"ok","message":"Nourotour webhook is active",...}
```
- در Apps Script دکمه Run روی تابع `testSave` بزنید، باید یک ردیف تست در شیت اضافه شود
- حالا از سایت یک فرم ارسال کنید

## 🔧 تغییرات انجام شده در سایت (index.html)

برای سازگاری با Apps Script:

```javascript
// قبل (اشتباه برای GAS - باعث preflight و CORS می‌شود):
headers: { "Content-Type": "application/json" }

// بعد (صحیح):
headers: { "Content-Type": "text/plain;charset=utf-8" }
```

`text/plain` باعث می‌شود مرورگر درخواست preflight (OPTIONS) نفرستد که Apps Script از آن پشتیبانی نمی‌کند.

## 📋 ساختار شیت
شیت به صورت خودکار این ستون‌ها را می‌سازد:
`id | kind | createdAt | fullName | phone | tour | destination | travelers | preferredDate | contactMethod | budget | message | pageUrl | userAgent`

## ❓ سوالات متداول

**Q: آیا لینک قبلی من کار می‌کند؟**
A: بله، لینک شما `.../exec` درست است، فقط کد داخل آن باید عوض شود و دوباره Deploy کنید (حتما New Version).

**Q: بعد از Deploy خطای CORS می‌گیرم؟**
A: مطمئن شوید Who has access = Anyone است و از `text/plain` استفاده می‌کنید (که در کد جدید سایت رعایت شده).

**Q: می‌خواهم به تلگرام هم بفرستم؟**
A: می‌توانید در `doPost` بعد از `appendRow` از `UrlFetchApp.fetch()` برای ارسال به تلگرام استفاده کنید.

---
برای هر مشکل دیگری لاگ‌ها را از Apps Script > Executions ببینید.
