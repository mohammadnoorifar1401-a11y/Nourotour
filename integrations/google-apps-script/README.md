# رلهٔ سروری درخواست‌ها: Apps Script → تلگرام (نسخهٔ ۳)

فرم‌های «مشاوره» و «رزرو» سایت، هر درخواست را با `POST` به نشانی `leadEndpoint` می‌فرستند
(نشانی پیش‌فرض در `index.html` → `defaultData.general.leadEndpoint`). آن نشانی یک وب‌اپ
**Google Apps Script** است و کدش همین‌جا است: [`Code.gs`](./Code.gs).

## چرا رله؟

قبلاً پیام تلگرام از داخل مرورگر بازدیدکننده به `api.telegram.org` فرستاده می‌شد؛ ولی این دامنه
از ایران فیلتر است، پس فرم «ثبت» می‌شد اما **هیچ پیامی نمی‌رسید**.
اکنون مسیر ارسال این است:

```
فرم سایت  →  وب‌اپ Apps Script (script.google.com، از ایران باز می‌شود)
          →  سرور گوگل  →  api.telegram.org  →  گروه تلگرام
```

هیچ داده‌ای در **Google Sheets** ثبت نمی‌شود؛ این اسکریپت فقط یک رلهٔ پیام است و هیچ توکنی هم
داخل مرورگر بازدیدکننده نیست.

## راه‌اندازی / به‌روزرسانی (از گوشی هم می‌شود)

1. کد [`Code.gs`](./Code.gs) را کامل کپی کنید (در گیت‌هاب: دکمهٔ **Raw** یا Copy).
2. بروید به [script.google.com](https://script.google.com) → پروژهٔ قبلی خود را باز کنید.
3. در `Code.gs`، **کل کد قبلی را پاک کنید** و کد جدید را بچسبانید → **ذخیره** (💾 / Ctrl+S).
4. از منوی بالا: **Deploy → Manage deployments → ✎ (Edit) → Version: New version → Deploy**
   و اگر پنجرهٔ دسترسی (Authorize) آمد، تأیید کنید.
5. یک‌بار تابع **`testPost`** را از منوی بالا انتخاب کنید و ▶ **Run** بزنید؛
   باید یک پیام آزمایشی (**🧪 ارسال آزمایشی**) در گروه تلگرام برسد و در Execution log بنویسد
   `{"status":"ok","telegram":true,…}`.

> **مهم:** بعد از هر تغییر کد، حتماً **New version** بدهید. بدون نسخهٔ جدید، نشانی `/exec`
> همچنان کد قدیمی (نسخهٔ Sheets) را اجرا می‌کند و پیام‌ها به تلگرام نمی‌رسند.
> تأیید سلامت نسخه: باز کردن نشانی `/exec` در مرورگر باید
> `{"status":"ok","service":"nourotour-telegram-relay","version":3,"telegram":true}` برگرداند.

## تنظیمات داخل `Code.gs`

| کلید | معنی |
|---|---|
| `BOT_TOKEN` | توکن ربات تلگرام (از BotFather) |
| `CHAT_ID` | شناسهٔ گروه/کانال مقصد (گروه: عدد منفی مثل `-1004463990161`) |
| `TIMEZONE` | منطقهٔ زمانی برای خط «زمان» پیام (`Asia/Tehran`) |
| `ALLOWED_KINDS` | انواع مجاز درخواست؛ پیش‌فرض `consult,booking,test` |

> 🔐 توکن ربات داخل مخزن عمومی است. اگر خواستید امن‌تر شود: در BotFather دستور `/revoke` را
> بزنید، توکن تازه را در `BOT_TOKEN` بگذارید و یک **New version** بدهید.

## تست اتصال

- **از داخل سایت:** `Ctrl + Shift + A` → تب «عمومی» → دکمهٔ **«ارسال درخواست آزمایشی»**.
  نتیجه همان‌جا نمایش داده می‌شود و باید پیام آزمایشی در گروه تلگرام برسد.
- **از ترمینال:**

  ```bash
  curl -L -X POST "https://script.google.com/macros/s/<ID>/exec" \
    -H "Content-Type: text/plain" \
    -d '{"kind":"consult","fullName":"تست","phone":"09120000000","destination":"استانبول"}'
  # → {"status":"ok","telegram":true,"messageId":123,"kind":"consult","kindLabel":"🔔 درخواست مشاوره"}
  ```

## چرا `text/plain`؟ (مشکل CORS)

Apps Script به درخواست **OPTIONS** (preflight) پاسخ نمی‌دهد. اگر مرورگر بدنه را با
`Content-Type: application/json` بفرستد، ابتدا preflight می‌فرستد، جواب نمی‌گیرد و درخواست را با خطای CORS
مسدود می‌کند؛ در این حالت `doPost` **اصلاً اجرا نمی‌شود** و در بخش *Executions* هیچ رکوردی از `doPost` دیده نمی‌شود.
به همین دلیل سایت برای نشانی‌های `script.google.com` بدنهٔ JSON را با `Content-Type: text/plain` می‌فرستد
(«درخواست ساده»، بدون preflight) و اسکریپت آن را از `e.postData.contents` می‌خواند.

## عیب‌یابی سریع

| نشانه | علت محتمل | راه‌حل |
|---|---|---|
| پیام در گروه تلگرام نمی‌رسد ولی فرم «ارسال شد» می‌شود | Deploy نسخهٔ قدیمی (Sheets) است | `Manage deployments → ✎ → New version → Deploy` |
| پاسخ `{"status":"error","message":"BOT_TOKEN یا CHAT_ID …"}` | تنظیمات خالی است | `BOT_TOKEN` و `CHAT_ID` را پر کنید |
| پاسخ `{"status":"error","message":"Forbidden…"}` یا `chat not found` | ربات عضو گروه نیست یا `CHAT_ID` غلط است | ربات را به گروه اضافه کنید و `CHAT_ID` را درست کنید |
| پاسخ `{"status":"error","message":"Unauthorized"}` | توکن باطل/اشتباه است | توکن BotFather را دوباره بگیرید |
| در Executions فقط `doGet` هست و `doPost` نیست | درخواست به اسکریپت نرسیده (CORS یا صفحهٔ کش‌شدهٔ قدیمی) | نسخهٔ فعلی سایت را منتشر کنید؛ صفحه را رفرش کامل کنید |
| پاسخ HTML/صفحهٔ ورود گوگل به‌جای JSON | دسترسی Deploy روی «Anyone with Google account» است | Deploy را با **Anyone** دوباره منتشر کنید |
| متن پیام با تگ‌های HTML رسیده | خطای قالب‌بندی HTML | اسکریپت خودش یک بار بدون `parse_mode` دوباره می‌فرستد؛ اگر ادامه داشت کد را به‌روز کنید |
| خطای اجرای `testPost`: «You do not have permission…» | مجوز `script.external_request` تأیید نشده | پنجرهٔ Authorize را تأیید کنید و `testPost` را دوباره بزنید |

## فیلدهای ارسالی

`id`, `kind` (`consult` / `booking` / `test`), `createdAt` (ISO), `createdAtFa` (شمسی), `pageUrl`, `site`,
و فیلدهای فرم: `fullName`, `phone`, `destination`, `budget`, `message`, `tour`, `travelers`, `preferredDate`, `contactMethod`.
پیام ساخته‌شده در سرور با HTML فرمت می‌شود و همهٔ مقادیر کاربر Escape می‌شوند.
