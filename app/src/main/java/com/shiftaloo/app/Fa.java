package com.shiftaloo.app;

import java.util.Locale;

public final class Fa {
    private static final char[] FA = {'۰','۱','۲','۳','۴','۵','۶','۷','۸','۹'};

    private Fa() {}

    public static String n(long value) {
        return digits(Long.toString(value));
    }

    public static String digits(String value) {
        if (value == null) return "";
        StringBuilder result = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            result.append(c >= '0' && c <= '9' ? FA[c - '0'] : c);
        }
        return result.toString();
    }

    public static String money(long value) {
        return n(value) + " تومان";
    }

    public static String time(int hour, int minute) {
        return digits(String.format(Locale.US, "%02d:%02d", hour, minute));
    }
}
