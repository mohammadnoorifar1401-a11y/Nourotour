# رلهٔ سروری درخواست‌ها: Apps Script → تلگرام (نسخهٔ ۴)

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

هیچ داده‌ای در **Google Sheets** ثبت نمی‌شود و هیچ توکنی در مرورگر یا مخزن این نسخه قرار ندارد.
توکن از Script properties سمت سرور خوانده می‌شود.

## راه‌اندازی / به‌روزرسانی (از گوشی هم می‌شود)

1. کد [`Code.gs`](./Code.gs) را کامل کپی کنید (در گیت‌هاب: دکمهٔ **Raw** یا Copy).
2. بروید به [script.google.com](https://script.google.com) و پروژهٔ قبلی خود را باز کنید.
3. در `Code.gs`، **کل کد قبلی را پاک کنید** و کد جدید را بچسبانید → **ذخیره** (💾).
4. در **Project Settings → Script properties**، کلید `BOT_TOKEN` را بسازید و توکن ربات را به‌عنوان مقدار وارد کنید.
   `CHAT_ID` اختیاری است؛ مقدار پیش‌فرض `-1004463990161` است و در صورت نیاز با Script property هم‌نام بازنویسی می‌شود.
5. از منوی توابع، **`checkConfig`** را انتخاب و ▶ **Run** کنید. در Execution log نتیجهٔ فارسی بررسی توکن و گروه را می‌بینید؛ این تابع فقط `getMe` و `getChat` را فراخوانی می‌کند و **هیچ پیامی به گروه نمی‌فرستد**. توکن در log ماسک می‌شود.
6. پس از موفقیت `checkConfig`، تابع **`testPost`** را اجرا کنید؛ باید پیام **🧪 ارسال آزمایشی** در گروه برسد و نتیجهٔ موفق/ناموفق همراه با راهنما در Execution log چاپ شود.
7. از منوی بالا **Deploy → Manage deployments → ✎ (Edit) → Version: New version → Deploy** را بزنید و اگر پنجرهٔ دسترسی (Authorize) آمد، تأیید کنید.

> **مهم دربارهٔ امنیت:** نسخه‌های قبلی این مخزن حاوی توکن ثابت بودند. اگر توکن قبلی هنوز فعال است، آن را در
> [@BotFather](https://t.me/BotFather) با `/revoke` باطل کنید و توکن تازه را فقط در Script properties بگذارید.
> `CONFIG.BOT_TOKEN` در `Code.gs` عمداً خالی است؛ توکن را داخل فایل کد ننویسید.
>
> بعد از هر تغییر کد حتماً **New version** بدهید؛ در غیر این صورت نشانی `/exec` همچنان نسخهٔ قبلی را اجرا می‌کند.

## بررسی سلامت و پیکربندی

باز کردن نشانی `/exec` در مرورگر، JSON بدون راز برمی‌گرداند. وقتی `BOT_TOKEN` در Script properties تنظیم باشد، پاسخ نمونه چنین است:

```json
{"status":"ok","service":"nourotour-telegram-relay","version":4,"token":"configured (script-properties)","chat":"configured (default)","telegram":true}
```

برای بررسی واقعی اعتبار توکن و دسترسی به گروه، از خود ویرایشگر Apps Script تابع **`checkConfig`** را اجرا کنید.
`doGet` وضعیت پیکربندی را نشان می‌دهد؛ `checkConfig` از تلگرام می‌پرسد و نتیجه را فارسی در Execution log ثبت می‌کند، بی‌آنکه پیامی ارسال کند.

## تنظیمات

| کلید | محل / معنی |
|---|---|
| `BOT_TOKEN` | **Project Settings → Script properties**؛ توکن ربات از BotFather. در `CONFIG` خالی می‌ماند. |
| `CHAT_ID` | **Script properties** اختیاری؛ شناسهٔ گروه/کانال مقصد. پیش‌فرض `-1004463990161`. |
| `TIMEZONE` | منطقهٔ زمانی برای خط «زمان» پیام (`Asia/Tehran`). |
| `ALLOWED_KINDS` | انواع مجاز درخواست؛ پیش‌فرض `consult,booking,test`. |

توکن پیش از استفاده از فاصله‌ها، نویسه‌های نامرئی و پیشوند احتمالی `bot` پاک‌سازی می‌شود؛ بنابراین چسباندن توکن با فاصله‌های اضافی هم مشکلی ایجاد نمی‌کند.

## دسترسی پنل مدیریت سایت

- **دسکتاپ:** `Ctrl + Shift + A`.
- **گوشی:** ۵ ضربهٔ پیاپی روی لوگوی هدر، در بازهٔ ۳ ثانیه.
- **پیوند مستقیم:** `https://nourotour.ir/#admin`، `https://nourotour.ir/#مدیریت` یا `https://nourotour.ir/?admin=1`.

در پنل، برای تست از تب «عمومی» دکمهٔ **«ارسال درخواست آزمایشی»** را بزنید. جزئیات `message` و `hint` پاسخ رله در تب «درخواست‌ها» برای هر رکورد ناموفق نمایش داده می‌شود.

## تست دستی از ترمینال

```bash
curl -L -X POST "https://script.google.com/macros/s/<ID>/exec" \
  -H "Content-Type: text/plain" \
  -d '{"kind":"consult","fullName":"تست","phone":"09120000000","destination":"استانبول"}'
# پاسخ موفق: {"status":"ok","telegram":true,"messageId":123,"kind":"consult",...}
# پاسخ خطا شامل status، message و hint فارسی است.
```

## چرا `text/plain`؟ (مشکل CORS)

Apps Script به درخواست **OPTIONS** (preflight) پاسخ نمی‌دهد. اگر مرورگر بدنه را با
`Content-Type: application/json` بفرستد، ابتدا preflight می‌فرستد، جواب نمی‌گیرد و درخواست را با خطای CORS
مسدود می‌کند؛ در این حالت `doPost` **اصلاً اجرا نمی‌شود** و در بخش *Executions* هیچ رکوردی از `doPost` دیده نمی‌شود.
به همین دلیل سایت برای نشانی‌های `script.google.com` بدنهٔ JSON را با `Content-Type: text/plain` می‌فرستد
(«درخواست ساده»، بدون preflight) و اسکریپت آن را از `e.postData.contents` می‌خواند.

## عیب‌یابی سریع

| نشانه | راه‌حل |
|---|---|
| `Unauthorized` | توکن revoke شده؛ از @BotFather توکن تازه بگیر و در Script properties بگذار. سپس `checkConfig` را دوباره اجرا کن. |
| `chat not found` | مقدار `CHAT_ID` و عضویت ربات در گروه را بررسی کن؛ ربات باید عضو همان گروه مقصد باشد. |
| خطای نداشتن اجازهٔ ارسال / `Forbidden` | ربات را عضو گروه کن و مجوز ارسال پیام را بررسی کن؛ در گروه‌های محدود، ممکن است مجوز مدیر لازم باشد. |
| `BOT_TOKEN` تنظیم نشده است | کلید `BOT_TOKEN` را در **Project Settings → Script properties** بساز؛ توکن را داخل کد نگذار. |
| `CHAT_ID` نامعتبر است | شناسهٔ مقصد را بررسی کن یا Script property را پاک کن تا مقدار پیش‌فرض اعمال شود. |
| کد درست است اما `/exec` رفتار قدیمی دارد | `Deploy → Manage deployments → ✎ → New version → Deploy` را انجام بده. |
| Executions فقط `doGet` دارد و `doPost` نیست | درخواست به اسکریپت نرسیده (معمولاً CORS یا صفحهٔ کش‌شدهٔ قدیمی)؛ دسترسی Deploy را روی **Anyone** بگذار و سایت را کامل تازه‌سازی کن. |
| پاسخ HTML/صفحهٔ ورود گوگل به‌جای JSON | دسترسی Deploy روی «Anyone with Google account» است؛ Deploy را با دسترسی **Anyone** دوباره منتشر کن. |
| متن پیام با تگ‌های HTML رسیده | برای خطای قالب‌بندی، اسکریپت خودش یک بار بدون `parse_mode` دوباره می‌فرستد. |
| `testPost` مجوز `script.external_request` می‌خواهد | پنجرهٔ Authorize را تأیید کن و `testPost` را دوباره اجرا کن. |

`checkConfig` فقط `getMe` و `getChat` را می‌خواند و هیچ پیام آزمایشی نمی‌فرستد؛ برای آزمایش ارسال، `testPost` را اجرا کنید.

## فیلدهای ارسالی

`id`, `kind` (`consult` / `booking` / `test`), `createdAt` (ISO), `createdAtFa` (شمسی), `pageUrl`, `site`,
و فیلدهای فرم: `fullName`, `phone`, `destination`, `budget`, `message`, `tour`, `travelers`, `preferredDate`, `contactMethod`.
پیام ساخته‌شده در سرور با HTML فرمت می‌شود و همهٔ مقادیر کاربر Escape می‌شوند.
