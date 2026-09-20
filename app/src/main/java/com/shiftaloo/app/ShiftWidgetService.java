package com.shiftaloo.app;

import android.content.Intent;
import android.widget.RemoteViewsService;

public final class ShiftWidgetService extends RemoteViewsService {
    @Override public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ShiftWidgetFactory(getApplicationContext(),intent.getIntExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID,0));
    }
}
