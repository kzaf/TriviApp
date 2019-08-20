package com.zaf.triviapp.service;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zaf.triviapp.R;
import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.widget.AppWidgetProvider;

import java.util.List;

public class AppWidgetService extends RemoteViewsService {

    private List<Scores> scoresList;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteListViewsFactory(getApplicationContext());
    }

    class RemoteListViewsFactory implements AppWidgetService.RemoteViewsFactory {

        final Context mContext;

        RemoteListViewsFactory(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            scoresList = AppWidgetProvider.scores;
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            if (scoresList == null) return 0;
            return scoresList.size();
        }

        @Override
        public RemoteViews getViewAt(int index) {

            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.scores_widget_item);
            Scores scores = scoresList.get(index);

            String widgetItem = scores.getCategoryName();

            views.setTextViewText(R.id.tv_scores_widget_item, widgetItem);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
