package com.shiftaloo.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class HolidayRepository {
    private static final Map<String, String> YEAR_1405;

    static {
        HashMap<String, String> h = new HashMap<>();
        put(h, 1, 1, "عید سعید فطر و عید نوروز");
        put(h, 1, 2, "تعطیل عید سعید فطر و عید نوروز");
        put(h, 1, 3, "عید نوروز");
        put(h, 1, 4, "عید نوروز");
        put(h, 1, 12, "روز جمهوری اسلامی ایران");
        put(h, 1, 13, "روز طبیعت");
        put(h, 1, 25, "شهادت امام جعفر صادق (ع)");
        put(h, 3, 6, "عید سعید قربان");
        put(h, 3, 14, "عید غدیر خم و رحلت امام خمینی");
        put(h, 3, 15, "قیام پانزده خرداد");
        put(h, 4, 3, "تاسوعای حسینی");
        put(h, 4, 4, "عاشورای حسینی");
        put(h, 5, 13, "اربعین حسینی");
        put(h, 5, 21, "رحلت رسول اکرم و شهادت امام حسن مجتبی (ع)");
        put(h, 5, 22, "شهادت امام رضا (ع)");
        put(h, 5, 30, "شهادت امام حسن عسکری (ع)");
        put(h, 6, 8, "ولادت رسول اکرم و امام جعفر صادق (ع)");
        put(h, 8, 22, "شهادت حضرت فاطمه زهرا (س)");
        put(h, 10, 2, "ولادت امام علی (ع) و روز پدر");
        put(h, 10, 16, "مبعث رسول اکرم (ص)");
        put(h, 11, 4, "ولادت حضرت قائم (عج)");
        put(h, 11, 22, "پیروزی انقلاب اسلامی ایران");
        put(h, 12, 9, "شهادت امام علی (ع)");
        put(h, 12, 19, "عید سعید فطر");
        put(h, 12, 20, "تعطیل عید سعید فطر");
        put(h, 12, 29, "روز ملی شدن صنعت نفت");
        YEAR_1405 = Collections.unmodifiableMap(h);
    }

    private static void put(Map<String, String> map, int month, int day, String title) {
        map.put(String.format(java.util.Locale.US, "1405-%02d-%02d", month, day), title);
    }

    public static String title(PersianDate date) {
        if (date.year == 1405) {
            String title = YEAR_1405.get(date.key());
            if (title != null) return title;
        }
        // تعطیلات ثابت خورشیدی برای سال‌های دیگر نیز کاملاً آفلاین قابل تشخیص‌اند.
        if (date.month == 1 && date.day >= 1 && date.day <= 4) return "عید نوروز";
        if (date.month == 1 && date.day == 12) return "روز جمهوری اسلامی ایران";
        if (date.month == 1 && date.day == 13) return "روز طبیعت";
        if (date.month == 3 && date.day == 14) return "رحلت امام خمینی";
        if (date.month == 3 && date.day == 15) return "قیام پانزده خرداد";
        if (date.month == 11 && date.day == 22) return "پیروزی انقلاب اسلامی ایران";
        if (date.month == 12 && date.day == 29) return "روز ملی شدن صنعت نفت";
        return null;
    }

    public static boolean isHoliday(PersianDate date) {
        return date.isFriday() || title(date) != null;
    }
}
