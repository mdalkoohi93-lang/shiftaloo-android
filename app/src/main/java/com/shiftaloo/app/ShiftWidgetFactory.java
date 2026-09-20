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
    private final int widget;
    private final List<Shift> rows = new ArrayList<>();

    public ShiftWidgetFactory(Context context,int widget) { this.context = context;this.widget=widget; }
    @Override public void onCreate() { load(); }
    @Override public void onDataSetChanged() { load(); }
    @Override public void onDestroy() { rows.clear(); }
    @Override public int getCount() { return rows.size(); }

    private void load() {
        rows.clear();
        android.content.SharedPreferences p=context.getSharedPreferences("widgets",Context.MODE_PRIVATE);
        long hospital=p.getLong(widget+"hospital",0);String type=p.getString(widget+"type","");
        try(DbHelper db=new DbHelper(context)){for(Shift s:db.futureShifts(System.currentTimeMillis(),10000))if((hospital==0||hospital==s.hospitalId)&&(type.isEmpty()||type.equals(s.type))){rows.add(s);if(rows.size()==30)break;}}
    }

    @Override public RemoteViews getViewAt(int position) {
        if (position < 0 || position >= rows.size()) return null;
        Shift shift = rows.get(position);
        PersianDate date = PersianDate.fromMillis(shift.startMillis);
        java.time.ZonedDateTime t = Instant.ofEpochMilli(shift.startMillis).atZone(ZoneId.systemDefault());
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_row);
        views.setTextViewText(R.id.widgetRowTitle, date.shortText() + " • " + shift.typeName());
        views.setTextViewText(R.id.widgetRowDetail, shift.hospitalName + " • ساعت " + Fa.time(t.getHour(), t.getMinute()));
        views.setOnClickFillInIntent(R.id.widgetRowRoot, new Intent().putExtra("shift_id", shift.id));
        return views;
    }

    @Override public RemoteViews getLoadingView() { return null; }
    @Override public int getViewTypeCount() { return 1; }
    @Override public long getItemId(int position) { return rows.get(position).id; }
    @Override public boolean hasStableIds() { return true; }
}
