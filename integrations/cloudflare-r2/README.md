# بکاپ خصوصی MONO روی Cloudflare R2 — پیش‌بررسی و راه‌اندازی

این پوشه پیاده‌سازی «طرح A» است: سایت روی GitHub Pages می‌ماند، و فقط
`api.nourotour.ir` یک Worker است که فایل بکاپ **رمزگذاری‌شده** را در یک bucket
**خصوصی** R2 نگه می‌دارد.

```
پنل مدیریت (مرورگر)
  ├─ ساخت JSON بکاپ (دادهٔ سایت + درخواست‌ها + سورس MONO)
  ├─ gzip
  ├─ AES-256-GCM با کلید PBKDF2-SHA256 از گذرواژهٔ کاربر
  └─ POST https://api.nourotour.ir/v1/backup
        Authorization: Bearer <token> + HMAC-SHA256 + timestamp
             ↓
        Worker (nourotour-backup)  ← فقط اعتبارسنجی و ذخیره
             ↓
        R2 bucket خصوصی «nourotour-mono-backups»
```

---

## ۱) پیش‌بررسی: آیا R2 روی حساب شما فعال می‌شود؟

اسکریپت آماده:

```bash
bash integrations/cloudflare-r2/check-r2.sh
```

سه چیز را بررسی می‌کند: ورود به حساب (`wrangler whoami`)، فعال‌بودن R2
(`wrangler r2 bucket list`) و اعتبار توکن API (در صورت تنظیم `CLOUDFLARE_API_TOKEN`).

### واقعیت‌هایی که باید بدانید (بر اساس مستندات Cloudflare)

| موضوع | وضعیت |
|---|---|
| فعال‌بودن پیش‌فرض | **خیر.** R2 یک «اشتراک» است که باید یک‌بار از داشبورد فعال شود |
| مسیر فعال‌سازی | داشبورد → **Storage & Databases → R2 → Overview → Add R2 subscription** |
| روش پرداخت | **الزامی است، حتی در سقف رایگان** (کارت معتبر + صورتحساب) |
| خطای معروف | `403` با کد **10042**: «Please enable R2 through the Cloudflare Dashboard» یعنی اشتراک R2 فعال نیست |
| نگه‌داشت موقت | هنگام افزودن کارت ممکن است یک hold موقت ۵ دلاری برای اعتبارسنجی اعمال شود که آزاد می‌شود |
| سقف رایگان | ۱۰ گیگابایت‌ماه فضای Standard، ۱ میلیون عملیات Class A، ۱۰ میلیون Class B در ماه، خروج داده رایگان |
| فراتر از سقف | به‌صورت خودکار از کارت کسر می‌شود؛ هشدار بودجه (Budget alert) مصرف را متوقف نمی‌کند |
| کشور/تحریم | کارت و آدرس صورتحساب باید از دید Cloudflare مجاز باشد؛ کارت‌های صادرشده در کشورهای تحریمی رد می‌شوند |

مرجع: `developers.cloudflare.com/r2/get-started/` و `developers.cloudflare.com/r2/pricing/`

### چک‌لیست دستی در داشبورد (فقط با حساب خودتان ممکن است)

- [ ] Billing → Payment methods: یک روش پرداخت فعال دیده می‌شود
- [ ] Storage & Databases → R2 → Overview: بدون دکمهٔ «Add R2 subscription» (یعنی فعال است)
- [ ] R2 → bucket → Settings: **Public Access = Disabled** و **r2.dev = Off**
- [ ] Workers & Pages: وجود `nourotour-backup` پس از `wrangler deploy`
- [ ] DNS: زون `nourotour.ir` در **همان حساب** باشد (شرط Custom Domain برای Worker)

> اگر روش پرداخت ندارید یا کارت شما پذیرفته نمی‌شود، **طرح جایگزین**: همان Worker با
> **Workers KV** به‌جای R2 (سقف رایگان ۱ گیگابایت، ۱٬۰۰۰ نوشتن در روز، بدون نیاز به
> اشتراک R2). بکاپ فعلی MONO حدود ۲۷۵ کیلوبایت است و در سقف ۲۵ مگابایتی هر کلید KV
> جا می‌شود. برای این کار فقط binding عوض می‌شود؛ توضیح در پایان همین فایل.

---

## ۲) راه‌اندازی (پس از تأیید دسترسی)

```bash
cd integrations/cloudflare-r2

# ۱) ورود و ساخت bucket خصوصی
npx wrangler login
npx wrangler r2 bucket create nourotour-mono-backups

# ۲) توکن آپلود (طولانی و تصادفی؛ فقط در Secret، هرگز در فایل)
node -e "console.log(require('crypto').randomBytes(32).toString('base64url'))"
npx wrangler secret put UPLOAD_TOKEN

# ۳) انتشار Worker + اتصال api.nourotour.ir
npx wrangler deploy

# ۴) بررسی سلامت
curl -s https://api.nourotour.ir/health
# {"ok":true,"service":"nourotour-backup","r2":true}
```

`scheduled` هر روز ۰۳:۰۰ UTC اجرا می‌شود و فقط `KEEP_OBJECTS` نسخهٔ آخر (پیش‌فرض ۶۰) را
نگه می‌دارد.

### دیدن فهرست و دانلود از سرور

```bash
TS=$(date +%s)
# امضا با همان توکن انجام می‌شود؛ برای سادگی از اسکریپت زیر استفاده کنید
node --input-type=module -e '
  // نمونهٔ کامل در integrations/secure-backup/core.js تابع signUpload
'
# یا از داخل پنل مدیریت، دکمهٔ «ارسال به سرور» و سپس فایل .ntbk را دانلود و با decrypt.mjs باز کنید
```

---

## ۳) مدل امنیتی

| لایه | پیاده‌سازی |
|---|---|
| محرمانگی بدنه | AES-256-GCM در مرورگر؛ کلید از PBKDF2-SHA256 با ۳۱۰٬۰۰۰ تکرار |
| کلید | فقط از گذرواژهٔ کاربر در همان نشست؛ در هیچ Storage و در هیچ فایلی ذخیره نمی‌شود |
| یکپارچگی | سربرگ ۴۰ بایتی به‌عنوان AAD + تگ GCM؛ هر تغییر یک بایت → شکست بازگشایی |
| احراز هویت آپلود | Bearer token تصادفی ۲۵۶ بیتی؛ مقایسهٔ زمان‌ثابت |
| ضد replay | `X-Backup-Timestamp` با پنجرهٔ ۵ دقیقه + `X-Backup-Signature` = HMAC-SHA256(token, ts + "\n" + sha256(body)) |
| دسترسی عمومی bucket | هیچ‌کدام؛ نه Custom Domain روی bucket، نه `r2.dev`، نه presigned URL عمومی |
| CORS | فقط `https://nourotour.ir` و `https://www.nourotour.ir` |
| حجم | سقف ۱۰ مگابایت در Worker؛ فرمت ورودی باید با `NTBK1` شروع شود |
| لاگ | هیچ توکن یا محتوایی لاگ نمی‌شود؛ فقط کد وضعیت |
| کلید در APK/مخزن | هیچ کلیدی در `index.html`، APK مونو یا این مخزن نیست؛ توکن فقط در Cloudflare Secret |

نکتهٔ صادقانه: کسی که به داشبورد Cloudflare شما دسترسی داشته باشد می‌تواند فایل‌ها را
حذف یا جایگزین کند. اما **بدون گذرواژه نمی‌تواند محتوا را بخواند**. پس گذرواژه را
جدا از حساب Cloudflare نگه دارید (مثلاً در یک مدیر رمز).

---

## ۴) هزینه در مقیاس این پروژه

بکاپ واقعی اندازه‌گیری‌شده در همین مخزن: **۲۷۵ کیلوبایت** (با سورس MONO) و
**۵.۴ کیلوبایت** (بدون سورس). با ۳۰ نسخه در ماه:

- فضای مصرفی: حدود **۸ مگابایت‌ماه** از سقف ۱۰٬۲۴۰ مگابایت
- نوشتن: ۳۰ عملیات Class A از ۱٬۰۰۰٬۰۰۰
- خواندن: فقط هنگام بازیابی

یعنی در سقف رایگان می‌مانید؛ هزینهٔ واقعی صفر است، به شرطی که اشتراک فعال شود.

---

## ۵) عیب‌یابی

| نشانه | علت | راه‌حل |
|---|---|---|
| `10042` هنگام deploy یا bucket list | اشتراک R2 فعال نیست | داشبورد → R2 → Add R2 subscription |
| `401 unauthorized` در پنل | توکن یا امضا نادرست/ساعت سیستم ناهمگام | Secret را بازتنظیم کنید؛ ساعت دستگاه را درست کنید |
| `415 not_ntbk1` | بدنه رمزشده نیست | از دکمهٔ «ارسال به سرور» همان پنل استفاده کنید |
| `413 too_large` | سورس MONO بزرگ‌تر از سقف | تیک «شامل سورس MONO» را بردارید یا سقف را در Worker بالا ببرید |
| CORS در Console | مبدأ در `ALLOWED_ORIGIN` نیست | مبدأ دقیق (با `https`) را به `wrangler.toml` اضافه کنید |
| Custom Domain وصل نمی‌شود | زون در حساب دیگر است | زون و Worker باید در یک حساب باشند |
| گواهی `api` صادر نمی‌شود | رکورد دستی/متعارض | رکورد `api` را حذف کنید و اجازه دهید Worker خودش رکورد بسازد |

---

## ۶) تست‌های این پوشه (بدون Cloudflare)

```bash
node integrations/cloudflare-r2/test-worker.mjs   # ۱۰ سناریو با R2 تقلبی
node integrations/secure-backup/test-core.mjs     # رمزگذاری/رمزگشایی/دست‌کاری/امضا
```

---

## ۷) طرح جایگزین بدون R2 (اگر اشتراک R2 فعال نشد)

اگر کارت بانکی اجازهٔ فعال‌سازی R2 نداد، همین Worker با KV هم کار می‌کند و
هیچ اشتراک پرداختی لازم ندارد:

```bash
npx wrangler kv namespace create MONO_BACKUPS
```

سپس در `wrangler.toml` به‌جای `[[r2_buckets]]`:

```toml
[[kv_namespaces]]
binding = "BACKUPS"
id = "<id>"
```

تفاوت‌های API که باید در `worker.js` استفاده شود:
`put(key, value)` (بدون `httpMetadata`)، `get(key, "arrayBuffer")`،
`list({ prefix, cursor })` و `delete(key)`. اندازه و تاریخ را باید در متادیتای
همان مقدار JSON یا در `metadata` با `put(..., { metadata })` نگه داشت.
سقف هر مقدار ۲۵ مگابایت و کل فضا ۱ گیگابایت است — برای بکاپ ۲۷۵ کیلوبایتی کافی است.
