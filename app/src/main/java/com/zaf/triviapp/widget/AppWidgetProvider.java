package com.zaf.triviapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.zaf.triviapp.R;
import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.service.WidgetService;
import com.zaf.triviapp.ui.SelectCategoryActivity;

import java.util.ArrayList;
import java.util.List;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

    public static final String WIDGET_UPDATED_SCORE = "WidgetUpdatedScore";
    public static List<Scores> scores = new ArrayList<>();
    private static String widgetTitle;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widget_title, widgetTitle);
        views.setRemoteAdapter(R.id.widget_list, new Intent(context, WidgetService.class));

        //Create an Intent with the AppWidgetManager.ACTION_APPWIDGET_UPDATE action//
        Intent intentUpdate = new Intent(context, AppWidgetProvider.class);
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        //Update the current widget instance only, by creating an array that contains the widget’s unique ID//
        int[] idArray = new int[]{appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
        //Wrap the intent as a PendingIntent, using PendingIntent.getBroadcast()//
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
        //Send the pending intent in response to the user tapping the ‘Update’ TextView//
        views.setOnClickPendingIntent(R.id.widget_layout, pendingUpdate);
        Intent intent = new Intent(context, SelectCategoryActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setPendingIntentTemplate(R.id.widget_title, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.hasExtra(WIDGET_UPDATED_SCORE)) {
            scores = intent.getParcelableArrayListExtra(WIDGET_UPDATED_SCORE);
            widgetTitle = context.getResources().getString(R.string.widget_title);
        } else {
            widgetTitle = context.getResources().getString(R.string.widget_title_no_data);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), AppWidgetProvider.class);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);

        if (appWidgetIds != null && appWidgetIds.length > 0) {
            onUpdate(context, appWidgetManager, appWidgetIds);
        }

        super.onReceive(context, intent);
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
