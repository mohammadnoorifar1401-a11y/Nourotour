# ⚡ راهنمای شروع سریع — رسیدن درخواست‌های فرم به گروه تلگرام

## 🎯 مسیر ارسال

```
فرم سایت  →  وب‌اپ Google Apps Script (رله، از ایران باز می‌شود)
          →  سرور گوگل  →  api.telegram.org  →  گروه تلگرام
```

> ⚠️ **مهم:** ارسال مستقیم از مرورگر بازدیدکننده به `api.telegram.org` **کار نمی‌کند**، چون این
> دامنه از ایران فیلتر است؛ فرم «ثبت» می‌شد ولی پیامی نمی‌رسید. به همین دلیل ارسال اکنون از
> سمت **سرور گوگل** انجام می‌شود. هیچ داده‌ای هم در Google Sheets ثبت نمی‌شود.

---

## 📝 کارهایی که باید انجام دهید

### ۱) سایت (انجام‌شده ✅)

- `index.html` → `defaultData.general.leadEndpoint` نشانی رله (همان `/exec` قبلی) را دارد.
- فرم‌های مشاوره و رزرو، رکورد را با `POST` به همین نشانی می‌فرستند
  (تابع `sendLeadToRelay`؛ `Content-Type: text/plain` تا خطای CORS ندهد).
- `telegram-integration.js` هم فقط کلاینت همین رله است؛ **هیچ توکنی داخل مرورگر نیست**.

### ۲) Apps Script روی گوشی/کامپیوتر (این مرحله را باید انجام دهید)

1. کد [`integrations/google-apps-script/Code.gs`](integrations/google-apps-script/Code.gs) را کامل کپی کنید.
2. بروید به [script.google.com](https://script.google.com) → **پروژهٔ قبلی** را باز کنید.
3. در `Code.gs` **کل کد قدیمی را پاک کنید** و کد جدید را بچسبانید → **ذخیره** 💾.
4. **Deploy → Manage deployments → ✎ → Version: New version → Deploy** (و تأیید دسترسی‌ها در صورت پرسش).
5. یک‌بار **`testPost`** را انتخاب کنید و ▶ **Run** بزنید → باید پیام آزمایشی **🧪 ارسال آزمایشی** در گروه برسد.

راهنمای کامل + عیب‌یابی: [`integrations/google-apps-script/README.md`](integrations/google-apps-script/README.md)

---

## 🧪 تست از داخل سایت

1. سایت را باز کنید.
2. `Ctrl + Shift + A` → تب «عمومی» → دکمهٔ **«ارسال درخواست آزمایشی»**.
3. پیام موفقیت («✅ پیام از سرور گوگل به تلگرام رسید») باید در پنل دیده شود و پیام در گروه تلگرام برسد.

## 🔧 اگر کار نکرد

| نشانه | راه‌حل |
|---|---|
| در پنل «✅ ارسال شد» می‌آید ولی در تلگرام خبری نیست | Deploy را با **New version** دوباره منتشر کنید (نسخهٔ قدیمی روی `/exec` باقی است) |
| پیام «❌ ناموفق» | متن خطا را ببینید: `BOT_TOKEN`/`CHAT_ID` یا عضو نبودن ربات در گروه |
| در Console مرورگر خطای CORS | مطمئن شوید نشانی با `script.google.com` شروع می‌شود و دسترسی Deploy روی **Anyone** است |

---

## 📂 فایل‌های مرتبط

| فایل | توضیح |
|---|---|
| `integrations/google-apps-script/Code.gs` | کد رله (Apps Script → تلگرام) |
| `integrations/google-apps-script/README.md` | راهنمای نصب و عیب‌یابی رله |
| `telegram-integration.js` | کلاینت رله در مرورگر (بدون توکن) |
| `integrations/telegram-simple.html` | صفحهٔ تست رله (اختیاری) |
