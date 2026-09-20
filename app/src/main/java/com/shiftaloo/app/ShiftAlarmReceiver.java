package com.shiftaloo.app;
import android.content.*;
/** Exact alarms start a foreground audio service; no network or server involved. */
public final class ShiftAlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL="shift_alarm_v2";
    @Override public void onReceive(Context context,Intent intent){
        long id=intent.getLongExtra("shift_id",0);
        try(DbHelper db=new DbHelper(context)){Shift shift=db.shift(id);if(shift==null||!shift.alarmEnabled)return;}
        context.startForegroundService(new Intent(context,AlarmSoundService.class).putExtra("shift_id",id));
    }
}
