# MONO v1.7.0 — اصلاح کرش شروع

گزارش خطای واقعی گوشی نشان داد خطا هنگام درخواست مجوز اعلان در `MainActivity.onCreate` رخ می‌داد: `IllegalArgumentException: Can only use lower 16 bits for requestCode`. درخواست خودکار مجوز از لحظهٔ شروع حذف شد تا برنامه باز شود. در صورت نیاز، اجازهٔ اعلان بعداً از بخش تنظیمات برنامه قابل درخواست است.

همچنین زمان‌بندی یادآورها در نبود مجوز زمان دقیق به حالت غیردقیق برمی‌گردد و خطای آن راه‌اندازی را متوقف نمی‌کند. لوگوی تطبیقی نیز به drawable مستقل متصل است.

## ساخت
```bash
./gradlew :app:assembleDebug --no-daemon
```
خروجی: `app/build/outputs/apk/debug/app-debug.apk`
