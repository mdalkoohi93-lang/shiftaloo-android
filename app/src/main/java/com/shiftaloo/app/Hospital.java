package com.shiftaloo.app;

public final class Hospital {
    public long id;
    public String name = "";
    public String ward = "";
    public long baseSalary;
    public long dayRate;
    public long eveningRate;
    public long nightRate;

    public long rateFor(String type) {
        if (Shift.TYPE_EVENING.equals(type)) return eveningRate;
        if (Shift.TYPE_NIGHT.equals(type)) return nightRate;
        return dayRate;
    }
}
