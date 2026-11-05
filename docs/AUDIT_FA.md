# گزارش بررسی فنی Panther

## محدوده و نقطه بازگشت

بررسی استاتیک روی نسخهٔ پایه `42e44af61baa52f7fa0139c6150a5bea3f2bbe7e` و تغییرات این شاخه انجام شد. ابتدای کار درخت Git پاک بود؛ این commit نقطهٔ بازگشت فایل‌های اولیه است. بررسی استاتیک جایگزین کامپایل، اجرای برنامه، تحلیل امنیتی پویا یا آزمون دستگاه نیست و نمی‌تواند وجود نداشتن خطاهای دیگر را ثابت کند.

## ساختار و نقش فایل‌ها

- `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `app/build.gradle.kts`, `gradle/wrapper/*`, `gradlew*`: مخازن، نسخه‌ها، پلاگین‌ها و تنظیمات ساخت را تعریف می‌کنند. سازگاری و دریافت وابستگی‌ها بدون اجرای Gradle تأیید نشده است.
- `app/proguard-rules.pro`: فعلاً قانون اختصاصی ندارد؛ minification در نسخهٔ release خاموش است.
- `AndroidManifest.xml`: نقطهٔ ورود، پشتیبانی RTL و غیرفعال بودن backup اندروید را مشخص می‌کند؛ در مانیفست اصلی مجوز اینترنت وجود ندارد. مانیفست ادغام‌شده بررسی نشده است.
- `PantherApplication.kt`, `MainActivity.kt`: راه‌اندازی Hilt، Compose، تم و جهت رابط. نمودار تزریق در زمان ساخت تأیید نشده است.
- `core/designsystem/components/PantherComponents.kt` و `core/designsystem/theme/{Color,Shape,Spacing,Theme,Type}.kt`: اجزای رابط و توکن‌ها. ارزیابی تصویری، کنتراست و دسترس‌پذیری روی دستگاه انجام نشده است.
- `core/navigation/PantherNavigation.kt`: مسیرهای اصلی، تنظیمات و جزئیات؛ مسیرهای ناوبری روی دستگاه آزمون نشده‌اند.
- `core/settings/ThemePreferences.kt`: تنظیمات DataStore؛ پایداری و بازیابی روی دستگاه آزمون نشده است.
- `data/local/Entities.kt`, `PantherDao.kt`, `PantherDatabase.kt`: موجودیت‌ها، روابط آبشاری، پرس‌وجوها و نسخهٔ اول Room. کامپایل پردازشگر Room و تولید schema بررسی نشده است.
- `data/repository/RoomPantherRepository.kt`: نگاشت، اعتبارسنجی و تراکنش فعال‌سازی، ترتیب و ست‌ها. تضمین قواعد تنها در مسیرهای استفاده‌کننده از مخزن اعمال می‌شود؛ آزمون تراکنش در دستگاه اجرا نشده است.
- `domain/model/Models.kt`, `domain/repository/PantherRepository.kt`, `domain/usecase/UseCases.kt`: مدل‌ها، اعتبارسنجی و قرارداد/کاربردهای دامنه. آزمون‌های Kotlin اجرا نشده‌اند.
- `di/PantherModule.kt`: فراهم‌سازی Room و binding مخزن؛ تأیید Hilt در زمان ساخت باقی مانده است.
- `feature/ProgramScreens.kt`, `ProfileScreens.kt`, `ViewModels.kt`: صفحه‌ها و جریان‌های حالت و عملیات. خطای جریان جزئیات و اسکرول در این تغییرات اصلاح شده‌اند، اما هنوز اجرا نشده‌اند.
- `values/strings.xml`, `values-fa/strings.xml`, `values/styles.xml`: منابع فارسی و تم پنجره؛ هر سه XML تجزیه شدند و ارجاعات رشته‌ای موجودند.
- `WorkoutValidationTest.kt`, `PantherDatabaseTest.kt`, `MainActivityTest.kt`: آزمون‌های موجود واحد، Room و راه‌اندازی؛ هیچ‌یک در این محیط اجرا نشده‌اند.
- `.github/workflows/android-ci.yml`, `.github/workflows/cleanup-artifacts.yml`: ساخت/آزمون و پاک‌سازی آرتیفکت. اجرای GitHub شمارهٔ `42061740445` در setup Android ناموفق بود؛ لاگ کامل در این محیط قابل دریافت نبود.
- `README.md`, `docs/ARCHITECTURE.md`, `docs/PRODUCT_SPECIFICATION.md`, `docs/ROADMAP.md`, `AGENTS.md`, `.gitignore`, قالب‌های GitHub: راهنما، محدودهٔ محصول و پیکربندی مخزن را مشخص می‌کنند.

## یافته‌ها

شمارهٔ خطوط موارد اصلاح‌شده مربوط به نسخهٔ پایه است.

| ردیف | مشکل | فایل و خط | شدت | اقدام | نتیجه |
|---|---|---|---|---|---|
| ۱ | جریان روزهای تمرین بدون مدیریت خطا در UI جمع‌آوری می‌شد؛ خطای Room ممکن بود موجب خرابی صفحه شود. | `feature/ProgramScreens.kt:130-131`, `feature/ViewModels.kt:47` | بالا | تبدیل خطا به وضعیت قابل نمایش | تغییر کد انجام شد؛ کامپایل و رفتار تأیید نشده‌اند. |
| ۲ | صفحهٔ جزئیات اسکرول نداشت؛ روزهای طولانی ممکن بود خارج از صفحه بمانند. | `feature/ProgramScreens.kt:25-26,133-143` | متوسط | افزودن اسکرول و وضعیت بارگذاری | تغییر کد انجام شد؛ بررسی دستگاه باقی مانده است. |
| ۳ | ساخت، sync و آزمون با نبود JDK و SDK مسدود است. | محیط ساخت، `gradle/wrapper/gradle-wrapper.properties:3` | بحرانی برای تحویل | فرمان‌های Gradle اجرا شدند اما قبل از شروع متوقف شدند؛ دسترسی به میزبان‌های توزیع Gradle و SDK نیز ناموفق بود. | حل‌نشده؛ APK تولید نشده است. |
| ۴ | CI در راه‌اندازی Android SDK ناموفق است. | `.github/workflows/android-ci.yml:26,60` | بالا | وضعیت job و annotationهای اجرای GitHub بررسی شد. | علت دقیق نامشخص؛ لاگ کامل در این محیط با EOF مواجه شد. |
| ۵ | کپی تصویر پروفایل بدون سقف حجم و پاک‌سازی تصاویر قبلی انجام می‌شود؛ می‌تواند باعث مصرف فضای ذخیره‌سازی شود. | `feature/ViewModels.kt:68-79` | متوسط | شناسایی استاتیک | حل‌نشده؛ تغییر مدیریت فایل نیازمند طراحی و آزمون سازگاری داده است. |
| ۶ | schema نسخهٔ اول Room در مخزن موجود نیست. | `app/build.gradle.kts:34`, `data/local/PantherDatabase.kt:6` | متوسط | بررسی فایل‌ها | پس از build موفق باید schema تولید و ثبت شود تا مبنای migration باشد. |

## آزمون‌ها و محدودیت‌ها

- تجزیهٔ TOML و همهٔ XMLهای اصلی، بررسی وجود ارجاعات `R.string`، نبود permission اینترنت در مانیفست اصلی و بررسی ZIP فایل wrapper انجام شد؛ موفق بود.
- `git diff --check` اجرا شد و خطای whitespace گزارش نکرد.
- فرمان‌های `./gradlew help`، `clean :app:assembleDebug`، `:app:testDebugUnitTest`، `:app:lintDebug` و `:app:connectedDebugAndroidTest` تلاش شدند؛ همگی به علت نبود Java پیش از شروع Gradle متوقف شدند.
- Android SDK، `adb` و دستگاه یا شبیه‌ساز در دسترس نبودند؛ نصب، اجرا، مجوزهای نهایی، امضا، اندازه و صحت APK قابل بررسی نبودند. هیچ APK در مسیر پروژه پیدا نشد.
- این محصول offline است و کد حساب کاربری یا اتصال API شبکه در محدودهٔ کد فعلی مشاهده نشد؛ امنیت وابستگی‌های دانلودنشده، محیط runtime و تجربهٔ UI تأیید نشده‌اند.

## قدم بعدی

در محیطی با JDK 17، Android SDK 36، دسترسی به مخازن Gradle/Google و شبیه‌ساز، فرمان‌های بالا را تکرار کنید؛ خطاهای واقعی build/CI را رفع و خروجی schema را ثبت کنید. پس از ساخت، صحت APK و امضا را با ابزارهای SDK بررسی و نصب و آزمون‌های دستگاه را اجرا کنید.
