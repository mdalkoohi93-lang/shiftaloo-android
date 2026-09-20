# شیفتالو — رابط نیتیو مطابق طرح تأییدشده

چهار صفحهٔ مرجع: صفحهٔ شیفت‌ها، تقویم، گزارش و فرم کشویی ثبت شیفت.
رنگ‌ها: کاغذی `#FFFAF5`، سرمه‌ای `#123968`، تمشکی `#DF2456`، نعناعی، هلویی، یاسی و آبی روشن.
فونت وزیرمتن و آیکن‌های Phosphor همراه مجوزهایشان در APK هستند. تصاویر هلوپرستار و بیمارستان با ابزار داخلی ImageGen از روی مرجع تولید شده‌اند؛ متن و کنترل‌های برنامه همگی View واقعی اندروید هستند.

## رفتار

- بدون ورود، اینترنت، WebView یا سرویس ابری؛ داده‌ها SQLite محلی هستند و backup ابری غیرفعال است.
- روزکار ۰۷:۰۰–۱۵:۰۰، عصرکار ۱۵:۰۰–۲۳:۰۰، شب‌کار ۲۳:۰۰–۰۷:۰۰؛ هر زمان تا دقت دقیقه قابل ویرایش است. پایان زودتر از شروع به روز بعد تعلق دارد. شروع و پایان برابر رد می‌شود.
- همهٔ تداخل‌ها با بیمارستان، نوع، تاریخ و ساعت نمایش داده می‌شوند؛ شیفت‌های پشت‌سرهم تداخل نیستند. هیچ الگوی تکرار وجود ندارد.
- نرخ خالی یا صفر، میانگین نرخ‌های غیرصفر بیمارستان است. اگر همه خالی باشند تخمین صفر است. مبلغ اختصاصی شیفت بر تعرفه مقدم است. حقوق ماهانه = حقوق پایه + مبالغ شیفت‌هایی که در ماه شروع شده‌اند. تغییر تعرفه، تخمین قدیمی بدون مبلغ اختصاصی را نیز تغییر می‌دهد.
- نمودار مبلغ و تعداد شیفت‌های ماه انتخابی و پنج ماه بعد از روی داده‌های واقعی، با واحد مشخص و اعداد فارسی بدون جداکننده.
- تقویم شمسی محاسباتی؛ جدول تعطیلات قمری بسته‌بندی‌شدهٔ ۱۴۰۵. برای سال‌های دیگر فقط تعطیلات ثابت و جمعه‌ها و توضیح محدودیت نشان داده می‌شود.
- آلارم AlarmManager با سرویس پخش foreground، قطع از اعلان یا صفحهٔ زنگ، توقف خودکار پس از پنج دقیقه و ثبت مجدد پس از روشن‌شدن گوشی. مجوز اعلان و هشدار دقیق و در اندرویدهای جدید نمایش تمام‌صفحه وابسته به تنظیمات گوشی‌اند.
- ویجت فهرستی با فیلتر مستقل بیمارستان و نوع شیفت برای هر ویجت؛ لمس عنوان برای تغییر فیلتر، لمس ردیف برای ویرایش.

## دارایی‌ها

- `app/src/main/res/drawable-nodpi/peach_nurse.png`: prompt: isolate/recreate the reference header's flat peach nurse, navy outline, mint leaf and clipboard, white cap with blue plus, transparent background, no text or UI.
- `app/src/main/res/drawable-nodpi/hospital_art.png`: prompt: isolate/recreate the reference pink card's pastel hospital building, mint trees, blue medical plus, transparent background, no text or UI.
- Both generated with built-in ImageGen; no CLI fallback.
- Sources: [Vazirmatn](https://github.com/rastikerdar/vazirmatn), [Phosphor](https://github.com/phosphor-icons/web), [Android Emulator Runner](https://github.com/ReactiveCircus/android-emulator-runner).

The deliverable is a debug-signed installable APK for direct device review, not a Play Store release. A stable private release signing key is required before production distribution; it must not be committed to the repository. No device recording, telemetry or user data is uploaded.
