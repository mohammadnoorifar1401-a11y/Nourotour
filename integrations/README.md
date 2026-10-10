# 🔌 ادغام‌های نوروتور

این پوشه شامل تمام ادغام‌ها و اتصالات سایت نوروتور به سرویس‌های خارجی است.

## 📁 ساختار پوشه

```
integrations/
├── google-apps-script/     # اتصال به Google Sheets
│   ├── Code.gs             # اسکریپت Apps Script
│   └── README.md           # راهنمای نصب
│
├── telegram/               # اتصال به تلگرام  
│   └── README.md           # مستندات کامل
│
├── telegram-notifier.js    # ماژول اصلی ارسال به تلگرام
├── telegram-simple.html    # صفحه تست تلگرام
├── INSTALL.md              # راهنمای نصب گام‌به‌گام
└── README.md               # این فایل
```

## 🎯 ادغام‌های موجود

### 1️⃣ Google Sheets (فعال ✅)

**وضعیت:** در حال اجرا  
**مسیر:** `google-apps-script/`  
**عملکرد:** ذخیره درخواست‌های فرم در Google Sheets

**اطلاعات بیشتر:** [`google-apps-script/README.md`](google-apps-script/README.md)

---

### 2️⃣ Telegram Bot (جدید 🆕)

**وضعیت:** آماده نصب  
**ربات:** [@nourotour_ir_bot](https://t.me/nourotour_ir_bot)  
**عملکرد:** ارسال خودکار درخواست‌ها به گروه تلگرام

**ویژگی‌ها:**
- ✅ ارسال فوری به گروه تلگرام
- ✅ فرمت زیبا با emoji و HTML
- ✅ عدم تداخل با Google Sheets
- ✅ کار در پس‌زمینه (non-blocking)

**فایل‌ها:**
- `telegram-notifier.js` - ماژول اصلی
- `telegram-simple.html` - صفحه تست
- [`INSTALL.md`](INSTALL.md) - راهنمای نصب کامل
- [`telegram/README.md`](telegram/README.md) - مستندات تکمیلی

**شروع سریع:**
```html
<!-- اضافه کردن به انتهای index.html -->
<script src="integrations/telegram-notifier.js"></script>
```

**راهنمای کامل:** [`INSTALL.md`](INSTALL.md)

---

## 🚀 نصب

برای نصب هر ادغام، به README مربوط به آن مراجعه کنید:

1. **Google Sheets:** [`google-apps-script/README.md`](google-apps-script/README.md)
2. **Telegram:** [`INSTALL.md`](INSTALL.md)

## 🧪 تست

### تست Google Sheets
```bash
# ارسال درخواست تست به Google Apps Script endpoint
curl -X POST "YOUR_GOOGLE_APPS_SCRIPT_URL" \
  -H "Content-Type: application/json" \
  -d '{"kind":"test","fullName":"تست","phone":"09120000000"}'
```

### تست Telegram
باز کردن فایل تست:
```
https://nourotour.ir/integrations/telegram-simple.html
```

## 📊 جریان داده

```
فرم سایت
    │
    ├──► Google Sheets (ذخیره دائمی)
    │
    └──► Telegram Group (اطلاع‌رسانی فوری)
```

## 🔐 امنیت

### توصیه‌های امنیتی:

1. **Google Sheets:**
   - ✅ فقط با ایمیل خودتان اجرا شود
   - ✅ دسترسی "Anyone" برای Web App (الزامی برای CORS)

2. **Telegram:**
   - ⚠️ توکن در کد جاوااسکریپت قابل مشاهده است
   - ✅ ربات را فقط در گروه خصوصی استفاده کنید
   - 🔒 برای امنیت بیشتر از Cloudflare Worker استفاده کنید

## 🛠️ ادغام‌های آینده

- [ ] Cloudflare Worker (proxy امن برای تلگرام)
- [ ] WhatsApp Business API
- [ ] اتصال به CRM
- [ ] Google Analytics Events
- [ ] Email notifications

## 📞 پشتیبانی

اگر سوال یا مشکلی دارید:
- 📝 [GitHub Issues](https://github.com/mohammadnoorifar1401-a11y/Nourotour/issues)
- 📧 Email: mohammad.noorifar.1401@gmail.com
- 💬 Telegram: در گروه نوروتور

## 📄 لایسنس

MIT License - استفاده آزاد برای پروژه‌های تجاری و شخصی

---

**نسخه:** 1.0.0  
**آخرین به‌روزرسانی:** ۱۴۰۳/۰۷/۱۹  
**سازگاری:** تمام مرورگرهای مدرن
