package com.shiftaloo.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.*;

/** Alarm display. Playback is owned by the foreground service, even when locked. */
public final class AlarmActivity extends Activity {
  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    setShowWhenLocked(true);
    setTurnScreenOn(true);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    long id = getIntent().getLongExtra("shift_id", 0);
    Shift shift;
    try (DbHelper db = new DbHelper(this)) {
      shift = db.shift(id);
    }
    Ui u = new Ui(this);
    LinearLayout root = u.col();
    root.setGravity(Gravity.CENTER);
    root.setPadding(u.dp(24), u.dp(40), u.dp(24), u.dp(40));
    root.setBackgroundColor(Ui.BG);
    root.addView(u.art(R.drawable.peach_nurse), u.full(170));
    TextView title = u.text("زنگ شیفتالو", 30, Ui.INK, true);
    title.setGravity(Gravity.CENTER);
    root.addView(title, u.gap(-2, 18));
    TextView detail =
        u.text(
            shift == null ? "زمان شیفت رسید" : shift.typeName() + " · " + shift.hospitalName,
            20,
            Ui.INK,
            true);
    detail.setGravity(Gravity.CENTER);
    root.addView(detail, u.gap(-2, 15));
    if (shift != null) {
      TextView date =
          u.text(
              MainActivity.dateText(PersianDate.fromMillis(shift.startMillis), true),
              15,
              Ui.MUTED,
              false);
      date.setGravity(Gravity.CENTER);
      root.addView(date, u.gap(-2, 12));
    }
    root.addView(u.button("قطع زنگ", Ui.CHECK, true, this::stop), u.gap(56, 32));
    setContentView(root);
  }

  void stop() {
    stopService(new Intent(this, AlarmSoundService.class));
    finish();
  }

  @Override
  public void onBackPressed() {
    stop();
  }
}
