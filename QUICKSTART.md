# ⚡ راهنمای شروع سریع - اتصال تلگرام

## 🎯 هدف
وصل کردن فرم‌های سایت به گروه تلگرام در **کمتر از ۱ دقیقه**!

---

## 📝 تنها کاری که باید انجام دهید

فایل `index.html` را باز کنید و **قبل از `</body>`** این خط را اضافه کنید:

```html
<script src="telegram-integration.js"></script>
</body>
</html>
```

**همین!** ✅

---

## 🧪 تست

1. سایت را باز کنید
2. Console را باز کنید (F12)
3. باید این پیام‌ها را ببینید:
   ```
   [Telegram] شروع راه‌اندازی اتصال تلگرام...
   [Telegram] ماژول تلگرام بارگذاری شد
   [Telegram] ✅ اتصال تلگرام فعال شد
   ```

4. فرم را پر کنید و ارسال کنید
5. پیام در گروه تلگرام دریافت می‌شود! 🎉

---

## 🔧 اگر کار نکرد

### مشکل: پیام "اتصال تلگرام فعال شد" را نمی‌بینم

**علت:** فایل بارگذاری نشده

**راه‌حل:**
1. مطمئن شوید فایل `telegram-integration.js` در روت سایت است
2. مسیر فایل را بررسی کنید:
   ```html
   <!-- اگر در پوشه دیگری است -->
   <script src="js/telegram-integration.js"></script>
   ```

### مشکل: فرم ارسال می‌شود ولی پیام در تلگرام دریافت نمی‌شود

**راه‌حل:**
1. Console را باز کنید
2. دنبال خطا بگردید
3. اگر خطای CORS یا Network دیدید، بگویید تا راه‌حل بدهیم

---

## 📂 فایل‌ها

| فایل | توضیح |
|------|-------|
| `telegram-integration.js` | فایل اصلی (آماده استفاده) |
| `integrations/telegram-simple.html` | صفحه تست |
| `integrations/INSTALL.md` | راهنمای کامل |

---

## 🌐 لینک فایل

فایل در GitHub شما:
```
https://github.com/mohammadnoorifar1401-a11y/Nourotour/blob/main/telegram-integration.js
```

برای استفاده مستقیم از GitHub (اگر فایل آپلود نمی‌کنید):
```html
<script src="https://cdn.jsdelivr.net/gh/mohammadnoorifar1401-a11y/Nourotour@main/telegram-integration.js"></script>
```

---

## ✅ چک‌لیست

- [ ] فایل `telegram-integration.js` در سایت آپلود شد
- [ ] خط `<script src="telegram-integration.js"></script>` به index.html اضافه شد
- [ ] سایت باز شد و Console بررسی شد
- [ ] پیام "اتصال تلگرام فعال شد" نمایش داده شد
- [ ] فرم تست ارسال شد
- [ ] پیام در گروه تلگرام دریافت شد

اگر همه ✅ است، **تبریک!** اتصال فعال است 🎉

---

## 📞 کمک

اگر مشکلی پیش آمد:
- GitHub: [مشکل جدید](https://github.com/mohammadnoorifar1401-a11y/Nourotour/issues)
- تلگرام: پیام در گروه
