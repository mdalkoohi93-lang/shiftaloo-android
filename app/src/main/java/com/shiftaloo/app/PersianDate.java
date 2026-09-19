package com.shiftaloo.app;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public final class PersianDate implements Comparable<PersianDate> {
    public static final String[] MONTHS = {
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    };
    public static final String[] WEEKDAYS = {
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
    };

    public final int year;
    public final int month;
    public final int day;

    public PersianDate(int year, int month, int day) {
        if (month < 1 || month > 12 || day < 1 || day > monthLength(year, month)) {
            throw new IllegalArgumentException("تاریخ شمسی نامعتبر است");
        }
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public static PersianDate today() {
        LocalDate g = LocalDate.now(ZoneId.systemDefault());
        return fromGregorian(g.getYear(), g.getMonthValue(), g.getDayOfMonth());
    }

    public static PersianDate fromMillis(long millis) {
        LocalDate g = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
        return fromGregorian(g.getYear(), g.getMonthValue(), g.getDayOfMonth());
    }

    public LocalDate toGregorian() {
        return jdnToGregorian(jalaliToJdn(year, month, day));
    }

    public long atTimeMillis(int hour, int minute) {
        return toGregorian().atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public PersianDate plusDays(long days) {
        LocalDate g = toGregorian().plusDays(days);
        return fromGregorian(g.getYear(), g.getMonthValue(), g.getDayOfMonth());
    }

    public int weekdayIndex() {
        // Saturday=0 … Friday=6
        int javaDay = toGregorian().getDayOfWeek().getValue(); // Monday=1 … Sunday=7
        return (javaDay + 1) % 7;
    }

    public boolean isFriday() { return weekdayIndex() == 6; }

    public String shortText() {
        return Fa.n(day) + " " + MONTHS[month - 1] + " " + Fa.n(year);
    }

    public String longText() {
        return WEEKDAYS[weekdayIndex()] + "، " + shortText();
    }

    public String key() {
        return String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    public static PersianDate parse(String key) {
        String[] parts = key.split("-");
        if (parts.length != 3) throw new IllegalArgumentException("تاریخ نامعتبر");
        return new PersianDate(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    public static boolean isLeap(int year) {
        return jalaliToJdn(year + 1, 1, 1) - jalaliToJdn(year, 1, 1) == 366;
    }

    public static int monthLength(int year, int month) {
        if (month <= 6) return 31;
        if (month <= 11) return 30;
        return isLeap(year) ? 30 : 29;
    }

    public static PersianDate fromGregorian(int year, int month, int day) {
        return jdnToJalali(gregorianToJdn(year, month, day));
    }

    private static final int[] BREAKS = {
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
            1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    };

    private static long div(long a, long b) { return a / b; }
    private static long mod(long a, long b) { return a - div(a, b) * b; }

    /** الگوریتم رسمی جلالی با نقاط شکست نجومی؛ برای سال‌های کاربردی تقویم ایران دقیق است. */
    private static int[] jalCal(int jy) {
        int gy = jy + 621;
        int leapJ = -14;
        int jp = BREAKS[0];
        int jump = 0;
        if (jy < jp || jy >= BREAKS[BREAKS.length - 1]) throw new IllegalArgumentException("سال خارج از محدوده تقویم است");
        for (int i = 1; i < BREAKS.length; i++) {
            int jm = BREAKS[i];
            jump = jm - jp;
            if (jy < jm) break;
            leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4);
            jp = jm;
        }
        int n = jy - jp;
        leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4);
        if (mod(jump, 33) == 4 && jump - n == 4) leapJ++;
        int leapG = (int) (div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150);
        int march = 20 + leapJ - leapG;
        if (jump - n < 6) n = n - jump + (int) div(jump + 4, 33) * 33;
        int leap = (int) mod(mod(n + 1, 33) - 1, 4);
        if (leap == -1) leap = 4;
        return new int[]{leap, gy, march};
    }

    private static long jalaliToJdn(int jy, int jm, int jd) {
        int[] r = jalCal(jy);
        return gregorianToJdn(r[1], 3, r[2]) + (jm - 1L) * 31 - div(jm, 7) * (jm - 7L) + jd - 1;
    }

    private static PersianDate jdnToJalali(long jdn) {
        LocalDate gregorian = jdnToGregorian(jdn);
        int jy = gregorian.getYear() - 621;
        int[] r = jalCal(jy);
        long firstFarvardin = gregorianToJdn(gregorian.getYear(), 3, r[2]);
        long k = jdn - firstFarvardin;
        if (k >= 0) {
            if (k <= 185) return new PersianDate(jy, 1 + (int) div(k, 31), 1 + (int) mod(k, 31));
            k -= 186;
        } else {
            jy--;
            k += 179;
            if (r[0] == 1) k++;
        }
        return new PersianDate(jy, 7 + (int) div(k, 30), 1 + (int) mod(k, 30));
    }

    private static long gregorianToJdn(int gy, int gm, int gd) {
        long d = div((gy + div(gm - 8L, 6) + 100100) * 1461, 4)
                + div(153 * mod(gm + 9L, 12) + 2, 5) + gd - 34840408;
        d = d - div(div(gy + 100100L + div(gm - 8L, 6), 100) * 3, 4) + 752;
        return d;
    }

    private static LocalDate jdnToGregorian(long jdn) {
        long j = 4 * jdn + 139361631;
        j = j + div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908;
        long i = div(mod(j, 1461), 4) * 5 + 308;
        int day = (int) (div(mod(i, 153), 5) + 1);
        int month = (int) (mod(div(i, 153), 12) + 1);
        int year = (int) (div(j, 1461) - 100100 + div(8 - month, 6));
        return LocalDate.of(year, month, day);
    }

    @Override public int compareTo(PersianDate other) { return this.toGregorian().compareTo(other.toGregorian()); }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersianDate)) return false;
        PersianDate that = (PersianDate) o;
        return year == that.year && month == that.month && day == that.day;
    }
    @Override public int hashCode() { return Objects.hash(year, month, day); }
    @Override public String toString() { return key(); }
}
