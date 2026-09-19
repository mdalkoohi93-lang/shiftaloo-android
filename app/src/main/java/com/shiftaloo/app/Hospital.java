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
        long explicit = Shift.TYPE_EVENING.equals(type) ? eveningRate : Shift.TYPE_NIGHT.equals(type) ? nightRate : dayRate;
        if (explicit > 0) return explicit;
        // All three default shifts are eight hours. Missing tariffs use the mean
        // of the entered tariffs; zero means unspecified, never a fabricated fee.
        long sum=0; int count=0;
        for(long rate : new long[]{dayRate,eveningRate,nightRate}) if(rate>0){sum+=rate;count++;}
        return count==0?0:Math.round((double)sum/count);
    }
}
