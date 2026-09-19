package com.shiftaloo.app;

public final class Shift {
    public static final String TYPE_DAY = "DAY";
    public static final String TYPE_EVENING = "EVENING";
    public static final String TYPE_NIGHT = "NIGHT";

    public long id;
    public long hospitalId;
    public String hospitalName = "";
    public String dateKey = "";
    public String type = TYPE_DAY;
    public long startMillis;
    public long endMillis;
    public long customAmount;
    public int reminderMinutes;
    public boolean alarmEnabled;
    public boolean paid;
    public String note = "";

    public String typeName() {
        if (TYPE_EVENING.equals(type)) return "عصرکار";
        if (TYPE_NIGHT.equals(type)) return "شب‌کار";
        return "روزکار";
    }
}
