package com.zaf.triviapp.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.zaf.triviapp.R;
import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.service.WidgetService;

import java.util.ArrayList;
import java.util.List;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

    public static List<Scores> scores = new ArrayList<>();
    private

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setRemoteAdapter(R.id.widget_list, new Intent(context, WidgetService.class));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}
