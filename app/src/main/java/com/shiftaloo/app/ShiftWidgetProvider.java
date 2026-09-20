package com.shiftaloo.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public final class ShiftWidgetProvider extends AppWidgetProvider {
    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            Intent service = new Intent(context, ShiftWidgetService.class);
            service.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
            service.setData(android.net.Uri.parse("shiftaloo://widget/"+id));
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_shiftaloo);
            views.setRemoteAdapter(R.id.widgetList, service);
            views.setEmptyView(R.id.widgetList, R.id.widgetEmpty);
            Intent open = new Intent(context, MainActivity.class);
            PendingIntent pending = PendingIntent.getActivity(context, 0, open,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widgetEmpty, pending);
            PendingIntent rowPending=PendingIntent.getActivity(context,id,new Intent(context,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
            views.setPendingIntentTemplate(R.id.widgetList, rowPending);
            Intent configure=new Intent(context,WidgetSettingsActivity.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);
            views.setOnClickPendingIntent(R.id.widgetHeader,PendingIntent.getActivity(context,id,configure,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
            manager.updateAppWidget(id, views);
            manager.notifyAppWidgetViewDataChanged(id, R.id.widgetList);
        }
    }

    public static void refresh(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, ShiftWidgetProvider.class));
        if (ids.length > 0) new ShiftWidgetProvider().onUpdate(context, manager, ids);
    }
}
