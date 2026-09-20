package com.shiftaloo.app;

import android.app.*;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import java.util.List;

/** Per-widget local filters, also available from the widget header. */
public final class WidgetSettingsActivity extends Activity {
  int widget;
  long hospital;
  String type = "";
  Ui u;
  TextView hospitalButton, typeButton;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    setResult(RESULT_CANCELED);
    widget = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
    if (widget < 0) {
      finish();
      return;
    }
    u = new Ui(this);
    SharedPreferences prefs = getSharedPreferences("widgets", MODE_PRIVATE);
    hospital = prefs.getLong(widget + "hospital", 0);
    type = prefs.getString(widget + "type", "");
    LinearLayout root = u.col();
    root.setGravity(Gravity.CENTER);
    root.setPadding(u.dp(24), u.dp(40), u.dp(24), u.dp(24));
    root.setBackgroundColor(Ui.BG);
    root.addView(u.art(R.drawable.peach_nurse), u.full(100));
    root.addView(u.text("ویجت شیفتالو", 24, Ui.INK, true), u.gap(55, 10));
    hospitalButton = u.text("", 15, Ui.INK, true);
    hospitalButton.setPadding(u.dp(14), 0, u.dp(14), 0);
    u.touch(hospitalButton, android.graphics.Color.WHITE, 14, Ui.LINE);
    hospitalButton.setOnClickListener(
        v -> {
          List<Hospital> hs = new DbHelper(this).hospitals();
          String[] names = new String[hs.size() + 1];
          names[0] = "همه بیمارستان‌ها";
          for (int i = 0; i < hs.size(); i++) names[i + 1] = hs.get(i).name;
          new AlertDialog.Builder(this)
              .setTitle("بیمارستان")
              .setItems(
                  names,
                  (d, w) -> {
                    hospital = w == 0 ? 0 : hs.get(w - 1).id;
                    labels();
                  })
              .show();
        });
    root.addView(hospitalButton, u.gap(55, 10));
    typeButton = u.text("", 15, Ui.INK, true);
    typeButton.setPadding(u.dp(14), 0, u.dp(14), 0);
    u.touch(typeButton, android.graphics.Color.WHITE, 14, Ui.LINE);
    typeButton.setOnClickListener(
        v ->
            new AlertDialog.Builder(this)
                .setTitle("نوع شیفت")
                .setItems(
                    new String[] {"همه شیفت‌ها", "روزکار", "عصرکار", "شب‌کار"},
                    (d, w) -> {
                      type =
                          new String[] {"", Shift.TYPE_DAY, Shift.TYPE_EVENING, Shift.TYPE_NIGHT}
                              [w];
                      labels();
                    })
                .show());
    root.addView(typeButton, u.gap(55, 10));
    root.addView(
        u.button(
            "ذخیرهٔ ویجت",
            Ui.CHECK,
            true,
            () -> {
              prefs
                  .edit()
                  .putLong(widget + "hospital", hospital)
                  .putString(widget + "type", type)
                  .apply();
              new ShiftWidgetProvider()
                  .onUpdate(this, AppWidgetManager.getInstance(this), new int[] {widget});
              setResult(
                  RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget));
              finish();
            }),
        u.gap(52, 20));
    labels();
    setContentView(root);
  }

  void labels() {
    Hospital h = new DbHelper(this).hospital(hospital);
    hospitalButton.setText(h == null ? "همه بیمارستان‌ها" : h.name);
    Shift s = new Shift();
    s.type = type;
    typeButton.setText(type.isEmpty() ? "همه شیفت‌ها" : s.typeName());
  }
}
