package com.shiftaloo.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Exact database aggregates, rendered locally. Unit is always explicit. */
public final class IncomeChartView extends View {
  long[] values = new long[0];
  String[] labels = new String[0];
  boolean count;
  Typeface font;
  Paint p = new Paint(3);

  public IncomeChartView(Context c) {
    super(c);
    setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
  }

  public void setData(long[] values, String[] labels, boolean count, Typeface font) {
    this.values = values;
    this.labels = labels;
    this.count = count;
    this.font = font;
    StringBuilder desc = new StringBuilder(count ? "تعداد شیفت‌ها" : "درآمد تخمینی به تومان");
    for (int i = 0; i < values.length; i++)
      desc.append("؛ ").append(labels[i].replace('\n', ' ')).append(": ").append(Fa.n(values[i]));
    setContentDescription(desc);
    invalidate();
  }

  float dp(float n) {
    return n * getResources().getDisplayMetrics().density;
  }

  @Override
  protected void onDraw(Canvas c) {
    super.onDraw(c);
    if (values.length == 0) return;
    float left = dp(42),
        top = dp(16),
        bottom = getHeight() - dp(37),
        width = getWidth() - left - dp(6);
    long max = 0;
    for (long v : values) max = Math.max(max, v);
    double unit = count ? 1 : 1_000_000.0;
    double ceiling = Math.max(count ? 4 : 1, Math.ceil(max / unit / 4) * 4);
    p.setTypeface(font);
    p.setTextSize(dp(9));
    p.setTextAlign(Paint.Align.RIGHT);
    for (int i = 0; i <= 4; i++) {
      float y = bottom - (bottom - top) * i / 4;
      p.setColor(Ui.LINE);
      p.setStrokeWidth(dp(.7f));
      c.drawLine(left, y, getWidth() - dp(4), y, p);
      p.setColor(Ui.MUTED);
      String label =
          Fa.digits(
              String.format(
                  java.util.Locale.US,
                  ceiling * i / 4 == Math.floor(ceiling * i / 4) ? "%.0f" : "%.1f",
                  ceiling * i / 4));
      c.drawText(label, left - dp(5), y + dp(3), p);
    }
    p.setTextAlign(Paint.Align.LEFT);
    p.setTextSize(dp(8));
    c.drawText(count ? "شیفت" : "میلیون تومان", 0, dp(9), p);
    float slot = width / values.length;
    for (int i = 0; i < values.length; i++) {
      float x = left + slot * i + slot * .18f,
          bar = slot * .64f,
          y = bottom - (float) (values[i] / unit / ceiling) * (bottom - top);
      p.setColor(i == 0 ? Ui.PINK : 0xfff7b9cf);
      c.drawRoundRect(x, y, x + bar, bottom, dp(3), dp(3), p);
      p.setColor(Ui.MUTED);
      p.setTextAlign(Paint.Align.CENTER);
      p.setTextSize(dp(9));
      String[] text = labels[i].split("\n");
      c.drawText(text[0], x + bar / 2, bottom + dp(16), p);
      if (text.length > 1) c.drawText(text[1], x + bar / 2, bottom + dp(29), p);
    }
    if (max == 0) {
      p.setTextSize(dp(12));
      p.setColor(Ui.MUTED);
      p.setTextAlign(Paint.Align.CENTER);
      c.drawText("هنوز داده‌ای برای نمودار نیست", getWidth() / 2f, top + (bottom - top) / 2, p);
    }
  }
}
