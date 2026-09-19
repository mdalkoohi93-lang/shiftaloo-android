package com.shiftaloo.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public final class IncomeChartView extends View {
    private long[] income = new long[0];
    private int[] shifts = new int[0];
    private String[] labels = new String[0];
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public IncomeChartView(Context context) { super(context); }
    public IncomeChartView(Context context, AttributeSet attrs) { super(context, attrs); }

    public void setData(long[] income, int[] shifts, String[] labels) {
        this.income = income;
        this.shifts = shifts;
        this.labels = labels;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (income.length == 0) return;
        float left = 36f, right = getWidth() - 18f, top = 34f, bottom = getHeight() - 54f;
        long max = 1;
        for (long value : income) max = Math.max(max, value);
        float slot = (right - left) / income.length;

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(24f);
        for (int i = 0; i < income.length; i++) {
            float x = left + slot * i + slot / 2;
            float height = (bottom - top) * income[i] / (float) max;
            paint.setColor(Color.rgb(255, 141, 121));
            canvas.drawRoundRect(x - slot * .25f, bottom - height, x + slot * .25f, bottom, 15, 15, paint);
            paint.setColor(Color.rgb(52, 42, 43));
            canvas.drawText(Fa.n(shifts[i]) + " شیفت", x, bottom - height - 10, paint);
            paint.setColor(Color.rgb(123, 110, 111));
            canvas.drawText(labels[i], x, bottom + 34, paint);
        }
    }
}
