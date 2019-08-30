package com.zaf.triviapp.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zaf.triviapp.R;
import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.widget.AppWidgetProvider;

import java.util.ArrayList;
import java.util.List;

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private List<Scores> scoresArrayList = new ArrayList<>();
    Context mContext;
    Intent intent;

    WidgetDataProvider(Context mContext, Intent intent) {
        this.mContext = mContext;
        this.intent = intent;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        scoresArrayList = AppWidgetProvider.scores;
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        if (scoresArrayList == null) return 0;
        return scoresArrayList.size();
    }

    @Override
    public RemoteViews getViewAt(final int index) {
        final RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.scores_widget_item);
        remoteViews.setTextViewText(R.id.tv_scores_widget_item, scoresArrayList.get(index).getCategoryName());
        remoteViews.setTextViewText(R.id.tv_scores_widget_item_score, scoresArrayList.get(index).getCategoryScore()* 10 + "%");

        return remoteViews;
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
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

}