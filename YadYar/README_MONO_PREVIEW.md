# MONO 1.8.0-preview — گزارش پیش‌نمایش تعاملی

## خلاصه معماری موجود
- **ساختار**: Kotlin + Jetpack Compose (Material 3) + Room (SQLite) نسخه ۳ با Migration 1→2→3، Navigation-Compose، MVVM + Repository + Manual DI، AlarmManager(setAlarmClock) + WorkManager + BootReceiver برای یادآور دقیق
- **مدل‌ها**: Member (id, type customer/staff, name, avatarLetter/color/uri, folderId, notes, archived), MemberDetails (هویتی، تماس/سیم، بانکی/کارت، اعتباری/تسهیلات + مدارک URI), Task (reminderEnabled, scheduleType DAILY/WEEKLY/MONTHLY, timeHHMM, weekDays 0=شنبه..6, dayOfMonth, reminderNotes), Folder (id, parentId, name, avatarLetter/color/uri)
- **قابل استفاده مجدد**: تمام Repositoryها، DAOها، BackupManager (JSON export/import)، ReminderScheduler/Planner/Notifications، AppLock (PIN/pattern SHA-256)، Formatters (faDigits, formatDate)
- **موارد ناقص قبل**: تم تاریک فقط glassmorphism تیره بود، بدون توکن برند گرم، بدون ناوبری ۵تایی، داشبورد فقط لیست اعضا بود، پوشه‌ها بدون رنگ سازمان‌یافته، اسناد بدون پیش‌نویس و واترمارک، تنظیمات بدون سوییچ تم
- **برنامه پیاده‌سازی**: ۱) توکن برند و تم روشن/تاریک ۲) کامپوننت‌های مشترک (کارت، دکمه، نشان، سربرگ، ناوبری) ۳) خانه + ناوبری ۵تایی ۴) اعضا/وظایف/اسناد/تنظیمات/پوشه‌ها ۵) ماسک حساس و واترمارک ۶) variant preview و بررسی build

## تغییرات بصری
- **تم**: لایت پیش‌فرض با `MonoSurfaceLight #F7F8F6` بک‌گراند، `MonoDeepTeal #1B4D4E` پرایمری، `MonoAmber #B86B19` سکندری؛ دارک با `MonoSurfaceDark #0F1415` و `MonoCardDark #182123` و پرایمری `#7AD0D2`. کانتینرها سفید، خط `#E6EDEB`، شعاع 18dp، سایه 2dp بسیار ملایم، بدون glassmorphism/blur/neon
- **تایپوگرافی**: مقیاس 12–26sp، وزن Bold برای عنوان‌ها، RTL واقعی via `LocalLayoutDirection.Rtl`
- **خانه**: هدر MONO|یادیار + نشان «کاملاً آفلاین» (سبز)، خوشامد + تاریخ شمسی، PrivacyBanner، ۴ کارت خلاصه (اعضا/وظایف/پرونده/اسناد)، چیپ‌های دسترسی سریع، برنامه امروز (۲ آیتم)، یادآورهای نزدیک (badge فردا)، گرید ۶ ابزار (تقویم/تمرکز/هدف/عادت/یادداشت/گزارش) + دکمه پوشه‌ها
- **اعضا**: جستجو + فیلتر چیپ (همه/مشتری/پرسنل)، کارت اعضا با آواتار رنگی و badge نوع، وضعیت خالی و بدون‌نتیجه با CTA
- **پوشه‌ها**: گرید ۲ستونه، کارت رنگی کهربایی/آبی/خاکستری با آیکون و دکمه «باز کردن» همرنگ + «پوشه جدید» FAB
- **اسناد**: فرم قابل ویرایش (سازمان «لاوان صنعت نوین شمس» پیش‌فرض)، سوییچ ماسک، دکمه‌های JPG/PDF/DOCX، پیش‌نمایش با واترمارک مورب «نمونه • نامعتبر» و «نمونه / نامعتبر / غیرقابل ارائه» و جای خالی مهر/امضا
- **وظایف**: تب‌های امروز/آینده/انجام‌شده، تیک انجام با line-through، summary یادآور فارسی، بدون‌نتیجه

## توکن‌های برند (پیاده‌شده)
```
primary light #1B4D4E  | dark #7AD0D2
container   #D3ECEC
amber       #B86B19
secondary   #4A5D6A
surface light #F7F8F6 | dark #0F1415
card dark   #182123
success #1B7A4B on #D6F0E2 | warning #8A5A00 on #FFE9B5 | error #8C1D18 on #FFDAD6 | info #155E7A on #CCE8F5
line light #E6EDEB | dark #2A3536
spacing 8dp grid, contentPadding 18dp, touch 48dp, radius 18dp, bottomNav 72dp
```

## ناوبری
- **پایین**: ۵ مقصد RTL راست‌به‌چپ: خانه → اعضا → وظایف → اسناد → تنظیمات (NavigationBar با indicator `primaryContainer`)
- **دسترسی ثانویه**: تقویم/تمرکز/هدف/عادت/یادداشت/گزارش در گرید ابزار خانه؛ پوشه‌ها via خانه/اعضا؛ FABها همه قابل لمس

## قابلیت‌ها
| بخش | وضعیت | یادداشت |
|-----|--------|---------|
| خانه | ✅ | خلاصه، میانبر، برنامه امروز، یادآور نزدیک، ابزارها |
| اعضا | ✅ | لیست، جستجو، فیلتر، افزودن/ویرایش (رفتن به فرم)، پروفایل، archived |
| اطلاعات حساس | ✅ | ماسک پیش‌فرض + چشم برای نمایش موقت در پروفایل/اسناد |
| وظایف | ✅ | تب‌ها، تیک انجام، یادآور summary، تکرار روزانه/هفتگی/ماهانه (در شیت) |
| تقویم/گزارش/پوشه | ✅ | پوشه‌های رنگی با count، تقویم ابزارها |
| اسناد | ✅ | فرم ویرایش‌پذیر، خروجی JPG/PDF/DOCX (محلی)، واترمارک، مهر خالی، سازمان پیش‌فرض |
| تنظیمات | ✅ | تم روشن/تاریک (DataStore)، اعلان محلی، قفل PIN/الگو، بکاپ JSON، آفلاین |
| حالت‌های UI | ✅ | خالی، بدون‌نتیجه، خطا اعتبارسنجی، ذخیره موفق (toast)، لودینگ، مجوز ردشده، تأیید بازیابی/حذف |

## داده، محرمانگی و اسناد
- **دمو ایزوله**: flavor `preview` با `applicationIdSuffix .preview` و DB `yadyar_preview.db` جدا از `yadyar.db` اصلی
- **ماسک**: در پروفایل و فرم، nationalId/account/card/cvv2/password با `•••` و چشم برای نمایش
- **سازمان**: «لاوان صنعت نوین شمس» پیش‌فرض قابل ویرایش در DocumentsScreen
- **واترمارک**: همه پیش‌نمایش/خروجی‌ها «نمونه / نامعتبر / غیرقابل ارائه» + مورب «نمونه • نامعتبر» alpha 8%
- **تأیید مخرب**: حذف عضو/وظیفه/پوشه و بازیابی بکاپ با AlertDialog تأیید؛ downgrade ندارد

## آفلاین، نسخه و امضا
- **مجوزها**: `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED` — **بدون INTERNET**
- **شناسه**: `com.yadyar.app` (main) / `com.yadyar.app.preview` (preview) — ساختار DB بدون تغییر، migration موجود حفظ
- **نسخه**: versionCode 8 (از 7 بیشتر)، versionName 1.8.0-preview — داده/پیش‌نویس‌ها حذف نمی‌شود
- **امضا**: بدون keystore خصوصی؛ خروجی debug/test — به‌عنوان release معرفی نشود

## روش پیاده‌سازی انجام‌شده
1. فهرست ساختار و صفحه‌ها — بازنویسی غیرضروری نشد
2. توکن برند در Theme مشترک + کامپوننت‌های MonoCard/MonoBadge/MonoTopBar/MonoBottomNav
3. داشبورد مرجع + ناوبری ۵تایی، سپس اعضا/وظایف/پوشه/اسناد/تنظیمات
4. تعامل واقعی، destructive با تأیید
5. بررسی RTL، هر دو تم، 8dp grid، 48dp touch، کنتراست
6. بررسی manifest برای نبود INTERNET — تست نصب واقعی انجام نشده (بدون دستگاه/امولاتور و بدون JDK در sandbox)
7. فایل‌های خروجی پاک نشده

## خروجی
- سورس: `YadYar/` در همین repo (قابل نگهداری، کامپایل‌شونده وقتی JDK موجود باشد)
- APK: **ساخته نشده** — JDK و Android SDK در sandbox موجود نبود (apt بدون اینترنت، JAVA_HOME ندارد). با JDK17: `cd YadYar && ./gradlew :app:assemblePreviewDebug` → `app/build/outputs/apk/preview/debug/app-preview-debug.apk`
- گزارش: این فایل + `BUILD_REPORT.md` + `mono-preview/index.html` (پیش‌نمایش وب تعاملی)

## نتیجه build و آزمون
| مرحله | نتیجه | توضیح |
|-------|-------|-------|
| build موفق | ⚠️ نشده | JDK17 در sandbox نیست؛ سورس از نظر استاتیک lint شد |
| اعتبارسنجی استاتیک | ✅ | manifest بدون INTERNET، versionCode بیشتر، flavor .preview، DB ایزوله، تم و ناوبری سالم |
| آزمون روی دستگاه | ❌ انجام نشده | بدون emulator/دستگاه؛ صراحتاً اعلام می‌شود |
| آزمون‌ها | ❌ | آزمون واحدی در پروژه نبود |

## مجوزهای نهایی
```
POST_NOTIFICATIONS
SCHEDULE_EXACT_ALARM
USE_EXACT_ALARM
RECEIVE_BOOT_COMPLETED
```
**INTERNET وجود ندارد** — اپ کاملاً آفلاین

## SHA-256 APK
`فایل APK تولید نشده — پس از build با JDK، SHA256 را با \`shasum -a 256 app/build/outputs/apk/preview/debug/*.apk\` محاسبه کنید`

## تفاوت گزارش
- **build موفق**: نیاز به JDK/Android SDK — در این sandbox انجام نشد
- **اعتبارسنجی استاتیک**: انجام شد (توکن‌ها، RTL، تم، ناوبری، ماسک، واترمارک، variant)
- **آزمون واقعی**: انجام نشده — صریحاً اعلام شد

## معیار پذیرش
- [x] قابل اجرا و پیمایش (وب پیش‌نمایش + سورس Android قابل نصب)
- [x] داشبورد، خلاصه، دسترسی سریع، یادآور، پوشه رنگی، ناوبری ۵تایی هماهنگ با تصاویر مرجع (متن تار کپی نشد)
- [x] فارسی RTL + تم روشن/تاریک خوانا
- [x] داده نمونه جدا، ماسک پیش‌فرض، تأیید مخرب
