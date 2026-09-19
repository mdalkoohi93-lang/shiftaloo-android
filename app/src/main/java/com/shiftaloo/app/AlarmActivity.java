package com.shiftaloo.app;

import android.app.Activity;
import android.app.NotificationManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class AlarmActivity extends Activity {
    private MediaPlayer player;
    private Vibrator vibrator;
    private long shiftId;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        shiftId = getIntent().getLongExtra("shift_id", 0);
        Shift shift = new DbHelper(this).shift(shiftId);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(36, 36, 36, 36);
        root.setBackgroundColor(Color.rgb(255, 248, 239));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        root.addView(logo, new LinearLayout.LayoutParams(220, 220));
        root.addView(label("زنگ شیفتالو", 30, true));
        root.addView(label(shift == null ? "زمان شیفت رسید" : shift.typeName() + " • " + shift.hospitalName, 22, true));
        if (shift != null) root.addView(label(PersianDate.fromMillis(shift.startMillis).longText(), 17, false));

        Button stop = new Button(this);
        stop.setText("قطع زنگ");
        stop.setTextSize(20);
        stop.setTextColor(Color.WHITE);
        stop.setBackgroundResource(R.drawable.bg_primary);
        stop.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 50, 0, 0);
        root.addView(stop, bp);
        setContentView(root);
        ring();
    }

    private TextView label(String text, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setGravity(Gravity.CENTER);
        v.setTextColor(Color.rgb(52, 42, 43));
        v.setPadding(0, 12, 0, 4);
        if (bold) v.setTypeface(v.getTypeface(), android.graphics.Typeface.BOLD);
        return v;
    }

    private void ring() {
        try {
            player = new MediaPlayer();
            player.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
            player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (Exception ignored) {}
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 700, 350}, 0));
    }

    @Override protected void onDestroy() {
        if (player != null) { player.stop(); player.release(); }
        if (vibrator != null) vibrator.cancel();
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel((int) shiftId);
        super.onDestroy();
    }
}
