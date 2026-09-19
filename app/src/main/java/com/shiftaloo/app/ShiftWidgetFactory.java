package com.shiftaloo.app;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public final class ShiftWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;
    private final List<Shift> rows = new ArrayList<>();

    public ShiftWidgetFactory(Context context) { this.context = context; }
    @Override public void onCreate() { load(); }
    @Override public void onDataSetChanged() { load(); }
    @Override public void onDestroy() { rows.clear(); }
    @Override public int getCount() { return rows.size(); }

    private void load() {
        rows.clear();
        rows.addAll(new DbHelper(context).futureShifts(System.currentTimeMillis() - 12 * 60 * 60 * 1000L, 20));
    }

    @Override public RemoteViews getViewAt(int position) {
        if (position < 0 || position >= rows.size()) return null;
        Shift shift = rows.get(position);
        PersianDate date = PersianDate.fromMillis(shift.startMillis);
        java.time.ZonedDateTime t = Instant.ofEpochMilli(shift.startMillis).atZone(ZoneId.systemDefault());
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_row);
        views.setTextViewText(R.id.widgetRowTitle, date.shortText() + " • " + shift.typeName());
        views.setTextViewText(R.id.widgetRowDetail, shift.hospitalName + " • ساعت " + Fa.time(t.getHour(), t.getMinute()));
        views.setOnClickFillInIntent(R.id.widgetRowTitle, new Intent().putExtra("shift_id", shift.id));
        return views;
    }

    @Override public RemoteViews getLoadingView() { return null; }
    @Override public int getViewTypeCount() { return 1; }
    @Override public long getItemId(int position) { return rows.get(position).id; }
    @Override public boolean hasStableIds() { return true; }
}
