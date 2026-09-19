package com.shiftaloo.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;

public final class ShiftAlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL = "shift_alarm";

    @Override public void onReceive(Context context, Intent intent) {
        long shiftId = intent.getLongExtra("shift_id", 0);
        Shift shift = new DbHelper(context).shift(shiftId);
        if (shift == null) return;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "زنگ شیفت‌ها", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("زنگ یادآوری شیفتالو");
            channel.enableVibration(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
            nm.createNotificationChannel(channel);
        }

        Intent alarm = new Intent(context, AlarmActivity.class).putExtra("shift_id", shiftId);
        PendingIntent fullScreen = PendingIntent.getActivity(context, (int) shiftId, alarm,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL)
                : new Notification.Builder(context);
        Notification notification = builder
                .setSmallIcon(com.shiftaloo.app.R.mipmap.ic_launcher)
                .setContentTitle("وقت " + shift.typeName() + " است")
                .setContentText(shift.hospitalName + " • " + PersianDate.fromMillis(shift.startMillis).shortText())
                .setCategory(Notification.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(fullScreen, true)
                .setContentIntent(fullScreen)
                .build();
        nm.notify((int) shiftId, notification);
        try { context.startActivity(alarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); } catch (Exception ignored) {}
    }
}
