package com.shiftaloo.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public final class AlarmScheduler {
    private AlarmScheduler() {}

    private static PendingIntent pending(Context context, long shiftId) {
        Intent intent = new Intent(context, ShiftAlarmReceiver.class);
        intent.putExtra("shift_id", shiftId);
        return PendingIntent.getBroadcast(context, (int) shiftId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void schedule(Context context, Shift shift) {
        cancel(context, shift.id);
        if (!shift.alarmEnabled) return;
        long when = shift.startMillis - shift.reminderMinutes * 60_000L;
        if (when <= System.currentTimeMillis()) return;
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent fire = pending(context, shift.id);
        Intent show = new Intent(context, MainActivity.class);
        PendingIntent showIntent = PendingIntent.getActivity(context, (int) shift.id, show,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            manager.setAlarmClock(new AlarmManager.AlarmClockInfo(when, showIntent), fire);
        } catch (SecurityException denied) {
            // تا زمان تأیید دسترسی «هشدار دقیق»، یک هشدار معمولی امن ثبت می‌شود.
            manager.set(AlarmManager.RTC_WAKEUP, when, fire);
        }
    }

    public static void cancel(Context context, long shiftId) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pending(context, shiftId));
    }
}
