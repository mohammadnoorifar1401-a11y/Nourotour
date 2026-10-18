# MONO v1.9.0 — نسخهٔ ۹

## هویت ساخت
- نام روی لانچر: `MONO`
- `versionCode = 9`، `versionName = 1.9.0`
- شناسهٔ برنامه: `com.mono.app`
- کاملاً آفلاین؛ مجوز `INTERNET` ندارد.

## امکانات این مرحله
- پوستهٔ ذغالی و طلایی، راست‌چین فارسی و هدر `MOHAMMAD NOORIFAR WORKSPACE`.
- آواتار ایموجی/گالری، فرم‌های متنی قابل‌ویرایش و جزئیات اعضا.
- علاقه‌مندی افراد/پوشه‌ها، حالت فهرست/جزئیات/شبکه و مرتب‌سازی مستقل پوشه‌ها.
- Room migration و JSON backup نسخهٔ ۲ با پوشه‌ها، تنظیمات نما، علاقه‌مندی‌ها و فیلدهای جدید.

## ساختار و اجرا
پروژهٔ کامل Kotlin/Jetpack Compose است. در Android Studio ریشهٔ پروژه را باز کنید و Gradle Sync را انجام دهید، یا از ترمینال اجرا کنید:

```bash
chmod +x gradlew
./gradlew :app:assembleDebug --no-daemon
```

APK در `app/build/outputs/apk/debug/app-debug.apk` ساخته می‌شود. `local.properties` و خروجی‌های `build/` داخل ZIP نیستند؛ Gradle/Android Studio آن‌ها را در دستگاه شما می‌سازند.
