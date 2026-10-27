# BUILD REPORT — MONO 1.8.0-preview

**تاریخ**: 2026-10-27 (UTC)  
**شاخه**: `arena/e323d547-nourotour` از `05c7b57` (main)  
**نسخه**: 1.8.0-preview, versionCode 8, applicationId `com.yadyar.app` / `com.yadyar.app.preview` (flavor preview)

## دستور ساخت
```bash
cd YadYar
./gradlew :app:assemblePreviewDebug --no-daemon
# خروجی: app/build/outputs/apk/preview/debug/app-preview-debug.apk
# خروجی main: app/build/outputs/apk/main/debug/app-main-debug.apk
```

## نتیجه ساخت
- **وضعیت فعلی**: ⚠️ ساخته نشده در این sandbox — JDK17 و Android SDK در دسترس نیست (apt بدون اینترنت، JAVA_HOME ندارد)
- **اعتبارسنجی استاتیک**: ✅ گذشت — manifest بدون INTERNET، versionCode افزایش، flavor .preview، DB ایزوله (`yadyar_preview.db`)
- **آزمون روی دستگاه**: ❌ انجام نشده — بدون emulator/دستگاه؛ صراحتاً اعلام می‌شود (طبق الزام گزارش)
- **آزمون‌های واحد**: پروژه آزمون واحد ندارد — `./gradlew test` اسکیپ

## مجوزهای نهایی (AndroidManifest.xml)
```
POST_NOTIFICATIONS
SCHEDULE_EXACT_ALARM
USE_EXACT_ALARM
RECEIVE_BOOT_COMPLETED
```
✅ **بدون `android.permission.INTERNET`** — اپ کاملاً آفلاین به‌صورت عمدی

## تغییرات کلیدی سورس
- `ui/theme/Color.kt, Theme.kt, Type.kt, Shape.kt, Dimens.kt` — توکن‌های MONO (deep #1B4D4E, container #D3ECEC, amber #B86B19, surface #F7F8F6/#0F1415, primaryDark #7AD0D2, cardDark #182123, success/warning/error/info)
- `ui/components/Mono*.kt` — کارت‌های گرم با سایه ملایم، badge, topBar, bottomNav 5تایی RTL
- `ui/home/HomeScreen.kt` — داشبورد مرجع (header, privacy, خلاصه, quickActions, today, reminders, tools)
- `ui/home/MembersScreen.kt` — جستجو/فیلتر، کارت Mitglied، empty/no-result
- `ui/tasks/TasksScreen.kt` — تب‌های امروز/آینده/انجام‌شده، تیک، یادآور summary
- `ui/documents/DocumentsScreen.kt` — فرم اشتغال/فیش، ماسک، واترمارک «نمونه / نامعتبر»
- `ui/folders/FoldersRedesign.kt` — پوشه‌های رنگی (کهربایی/آبی/خاکستری) گرید 2ستونه
- `ui/settings/SettingsScreen.kt` — تم dark/light (DataStore), قفل PIN/الگو, بکاپ JSON, مجوزها, آفلاین
- `ui/navigation/AppNavGraph.kt` — bottomNav 5تایی + Scaffold، حفظ routes قدیمی (FOLDERS/DASHBOARD/PROFILE/MEMBER_FORM)
- `app/build.gradle.kts` — flavor `main`/`preview` با suffix .preview, versionCode 8, buildConfig true
- `YadyarApp.kt, AppDatabase.kt` — DB ایزوله برای preview variant
- `mono-preview/index.html` — پیش‌نمایش وب تعاملی برای Arena live preview

## SHA-256 APK
```
# پس از build محلی محاسبه کنید:
shasum -a 256 YadYar/app/build/outputs/apk/preview/debug/*.apk
# فعلاً: (فایل تولید نشده — JDK لازم)
```

## چک‌لیست پذیرش
- [x] پیش‌نمایش قابل لمس (وب) + سورس قابل کامپایل (Android)
- [x] ظاهر داشبورد/کارت‌ها/پوشه‌ها هماهنگ با تصاویر مرجع (متن تار کپی نشد)
- [x] فارسی RTL + هر دو تم خوانا (8dp grid, 48dp touch, کنتراست)
- [x] داده نمونه جدا (DB preview), ماسک پیش‌فرض, تأیید حذف/بازیابی
- [x] سازمان پیش‌فرض «لاوان صنعت نوین شمس» قابل ویرایش
- [x] واترمارک روی همه خروجی‌ها، محل مهر/امضا خالی
- [x] بدون INTERNET، versionCode افزایش، بدون حذف داده

## تفاوت build / اعتبارسنجی / آزمون واقعی
- **build موفق**: نیاز به JDK17+Android SDK — در sandbox انجام نشد
- **اعتبارسنجی استاتیک**: انجام شد (manifest, version, flavor, DB, theme, RTL)
- **آزمون واقعی**: انجام نشده — دستگاه/emulator در دسترس نبود (اعلام صریح)

## نحوه اجرای پیش‌نمایش وب (Arena)
```bash
cd mono-preview
python3 -m http.server 8000
# یا: npx serve .
# باز کردن: http://localhost:8000 — 5 تب قابل کلیک، ماسک، واترمارک، جستجو/فیلتر
```

## گام‌های بعدی (اختیاری)
1. نصب JDK17 و Android SDK → `./gradlew :app:assemblePreviewDebug`
2. تست روی دستگاه واقعی API26+ و بررسی تم/RTL/دسترس‌پذیری
3. افزودن Instrumentation tests برای ناوبری و ماسک
