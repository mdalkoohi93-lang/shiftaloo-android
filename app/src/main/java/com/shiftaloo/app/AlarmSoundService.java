package com.shiftaloo.app;
import android.app.*;
import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.*;

public final class AlarmSoundService extends Service {
    MediaPlayer player; Vibrator vibrator; final Handler handler=new Handler(Looper.getMainLooper());
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent==null||"STOP".equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
        long id=intent.getLongExtra("shift_id",0);Shift shift;try(DbHelper db=new DbHelper(this)){shift=db.shift(id);}if(shift==null){stopSelf();return START_NOT_STICKY;}
        NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);NotificationChannel channel=new NotificationChannel(ShiftAlarmReceiver.CHANNEL,"زنگ شیفت‌ها",NotificationManager.IMPORTANCE_HIGH);channel.setSound(null,null);channel.enableVibration(false);manager.createNotificationChannel(channel);
        Intent show=new Intent(this,AlarmActivity.class).putExtra("shift_id",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent screen=PendingIntent.getActivity(this,(int)id,show,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop=PendingIntent.getService(this,0,new Intent(this,AlarmSoundService.class).setAction("STOP"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notification=new Notification.Builder(this,ShiftAlarmReceiver.CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("یادآوری "+shift.typeName()).setContentText(shift.hospitalName).setCategory(Notification.CATEGORY_ALARM).setOngoing(true).setFullScreenIntent(screen,true).setContentIntent(screen).addAction(new Notification.Action.Builder(null,"قطع زنگ",stop).build()).build();
        startForeground(7001,notification);releaseAudio();
        try{Uri sound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);if(sound==null)sound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);player=new MediaPlayer();player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());player.setDataSource(this,sound);player.setWakeMode(this,PowerManager.PARTIAL_WAKE_LOCK);player.setLooping(true);player.prepare();player.start();}catch(Exception e){releaseAudio();}
        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);if(vibrator!=null)vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0,700,350},0));handler.removeCallbacksAndMessages(null);handler.postDelayed(this::stopSelf,5*60*1000L);return START_NOT_STICKY;
    }
    void releaseAudio(){if(player!=null){player.release();player=null;}if(vibrator!=null){vibrator.cancel();vibrator=null;}}
    @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);releaseAudio();stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();}
}
