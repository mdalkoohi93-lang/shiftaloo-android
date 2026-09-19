package com.shiftaloo.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        DbHelper db = new DbHelper(context);
        for (Shift shift : db.futureShifts(System.currentTimeMillis(), 500)) {
            if (shift.alarmEnabled) AlarmScheduler.schedule(context, shift);
        }
        ShiftWidgetProvider.refresh(context);
    }
}
