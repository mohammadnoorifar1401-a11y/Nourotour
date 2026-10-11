# 🔌 ادغام‌های نوروتور

این پوشه شامل ادغام‌های سایت نوروتور با سرویس‌های خارجی است.

## 📁 ساختار

```
integrations/
├── google-apps-script/     # رلهٔ سروری: Apps Script → تلگرام (مسیر اصلی و فعال ✅)
│   ├── Code.gs             # کد رله (بدون Google Sheets)
│   └── README.md           # راهنمای نصب/به‌روزرسانی و عیب‌یابی
│
├── telegram/README.md      # مستندات قدیمی (منسوخ؛ به‌جایش google-apps-script/README.md)
├── telegram-notifier.js    # کلاینت قدیمی رله (سازگاری با صفحه‌های قدیمی)
├── telegram-simple.html    # صفحهٔ تست رله (اختیاری)
├── INSTALL.md              # راهنمای قدیمی (منسوخ)
└── README.md               # این فایل
```

## 🎯 مسیر فعال ارسال درخواست‌ها

```
فرم سایت → وب‌اپ Apps Script (/exec در script.google.com) → سرور گوگل → api.telegram.org → گروه تلگرام
```

**چرا این‌طور؟** قبلاً پیام تلگرام از مرورگر بازدیدکننده به `api.telegram.org` فرستاده می‌شد؛ ولی این
دامنه از ایران فیلتر است، پس فرم ثبت می‌شد و **پیامی نمی‌رسید**. حالا ارسال از سمت سرور گوگل انجام
می‌شود. Google Sheets هم دیگر در مسیر نیست و هیچ داده‌ای در آن ثبت نمی‌شود.

- کد رله: [`google-apps-script/Code.gs`](google-apps-script/Code.gs)
- راهنمای راه‌اندازی/به‌روزرسانی و عیب‌یابی: [`google-apps-script/README.md`](google-apps-script/README.md)
- کلاینت مرورگر: [`../telegram-integration.js`](../telegram-integration.js) — فقط درخواست را به رله می‌دهد؛ **هیچ توکن تلگرامی در مرورگر نیست**.

## 🧪 تست

1. از داخل سایت: `Ctrl + Shift + A` → تب «عمومی» → **«ارسال درخواست آزمایشی»**.
2. یا صفحهٔ تست: [`telegram-simple.html`](telegram-simple.html).

## ⚠️ فایل‌های منسوخ

`telegram/README.md` و `INSTALL.md` مربوط به دورهٔ قبل‌اند (ارسال مستقیم مرورگر → تلگرام که از ایران
کار نمی‌کرد). برای نصب فعلی فقط از راهنمای `google-apps-script/README.md` استفاده کنید.
