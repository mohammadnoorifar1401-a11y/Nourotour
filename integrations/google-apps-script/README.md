# دریافت درخواست‌های فرم در Google Sheets (Apps Script)

فرم‌های «مشاوره» و «رزرو» سایت، هر درخواست را با `POST` به نشانی `leadEndpoint` می‌فرستند.
نشانی پیش‌فرض داخل `index.html` (`defaultData.general.leadEndpoint`) یک وب‌اپ Google Apps Script
است که رکورد را در یک Google Sheet ذخیره می‌کند. کد آن در همین پوشه است: [`Code.gs`](./Code.gs).

## راه‌اندازی / به‌روزرسانی اسکریپت

1. Google Sheets → فایل جدید (مثلاً **Nourotour Leads**) → منوی **Extensions → Apps Script**.
2. محتوای `Code.gs` را با فایل همین پوشه جای‌گزین و ذخیره کنید (Ctrl+S).
3. در نوار بالا تابع **`testPost`** را انتخاب و ▶ **Run** بزنید → پنجرهٔ مجوز را تأیید کنید.
   باید یک ردیف «ارسال آزمایشی» در برگهٔ `Leads` ظاهر شود و در Execution log بنویسد `{"status":"ok",…}`.
4. **Deploy → New deployment** → نوع **Web app**:
   - Execute as: **Me**
   - Who has access: **Anyone** (نه «Anyone with Google account»)
5. نشانی `…/exec` را کپی کنید و اگر با مقدار `leadEndpoint` در `index.html` فرق دارد، آن را جای‌گزین کنید.

> **مهم:** بعد از هر تغییر کد، **Deploy → Manage deployments → ✎ → Version: New version → Deploy**.
> بدون نسخهٔ جدید، نشانی `/exec` همچنان کد قدیمی را اجرا می‌کند.
> اگر `doGet` نسخهٔ جدید فعال باشد، باز کردن نشانی در مرورگر `{"status":"ok","service":"nourotour-leads","version":2}` برمی‌گرداند.

## تست اتصال

- **از داخل سایت:** `Ctrl + Shift + A` → تب «عمومی» → دکمهٔ **«ارسال درخواست آزمایشی»**. نتیجه و پاسخ سرویس همان‌جا نمایش داده می‌شود
  و یک ردیف «ارسال آزمایشی» به Sheet اضافه می‌شود.
- **از ترمینال:**

  ```bash
  curl -L -X POST "https://script.google.com/macros/s/<ID>/exec" \
    -H "Content-Type: text/plain" \
    -d '{"kind":"consult","fullName":"تست","phone":"09120000000","destination":"استانبول"}'
  # → {"status":"ok","id":"","row":N}
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
| در Executions فقط `doGet` هست و `doPost` نیست | درخواست به اسکریپت نرسیده: CORS (نسخهٔ قدیمی سایت با `application/json`) یا نشانی خالی در مرورگر بازدیدکننده | نسخهٔ فعلی سایت را منتشر کنید؛ نشانی در `index.html` سراسری است |
| `doPost` با وضعیت **Failed** | خطای داخل اسکریپت (مجوز Sheets، `openById` اشتباه، JSON نامعتبر) | متن خطا را ببینید؛ `testPost` را اجرا کنید تا مجوزها تأیید شود |
| پاسخ HTML/صفحهٔ ورود گوگل به‌جای JSON | دسترسی Deploy روی «Anyone with Google account» است | Deploy را با **Anyone** دوباره منتشر کنید |
| پاسخ `{"status":"ok"}` ولی Sheet خالی | Deploy نسخهٔ قدیمی است یا `SHEET_ID` به فایل دیگری اشاره می‌کند | New version بدهید / `SHEET_ID` را خالی بگذارید تا Sheet متصل استفاده شود |
| شمارهٔ موبایل بدون صفر اول | ستون متنی نیست | اسکریپت ستون‌ها را `@` (متن) می‌کند؛ برای برگه‌های قدیمی دستی Format → Plain text |

## فیلدهای ارسالی

`id`, `kind` (`consult` / `booking` / `test`), `createdAt` (ISO), `createdAtFa` (شمسی), `pageUrl`, `site`,
و فیلدهای فرم: `fullName`, `phone`, `destination`, `budget`, `message`, `tour`, `travelers`, `preferredDate`, `contactMethod`.
فیلدهای ناشناخته در ستون «سایر فیلدها (JSON)» ذخیره می‌شوند تا با تغییر فرم چیزی گم نشود.
