package com.zaf.triviapp.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.zaf.triviapp.R;
import com.zaf.triviapp.database.tables.Scores;

import java.util.ArrayList;
import java.util.List;

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    public static final String DATA_SCORES = "DataScores";
    public static final String SCORES_BY_USER = "ScoresByUser";
    public static final String SCORE = "Score";
    private List<Scores> scoresList = new ArrayList<>();
    String userId;
    DatabaseReference childNode;
    Context mContext;
    Intent intent;

    WidgetDataProvider(Context mContext, Intent intent) {
        this.mContext = mContext;
        this.intent = intent;
    }

    @Override
    public void onCreate() {
        initializeData();
    }

    @Override
    public void onDataSetChanged() {
        initializeData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return scoresList.size();
    }

    @Override
    public RemoteViews getViewAt(int index) {

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.scores_widget_item);
        remoteViews.setTextViewText(R.id.tv_scores_widget_item, scoresList.get(index).getCategoryName());
        remoteViews.setTextViewText(R.id.tv_scores_widget_item_score, scoresList.get(index).getCategoryScore()* 10 + "%");

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
        return true;
    }

    private void initializeData() throws NullPointerException {
        try {
            scoresList.clear();
            assert FirebaseAuth.getInstance().getCurrentUser() != null;
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            childNode = FirebaseDatabase.getInstance().getReference(DATA_SCORES).child(SCORES_BY_USER).child(userId);
            childNode.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (final DataSnapshot category : dataSnapshot.getChildren()) {
                        scoresList.add(new Scores(userId, category.getKey(), Integer.parseInt(category.child(SCORE).getValue().toString())));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (NullPointerException e){
            Log.d("firebase_exception", "initializeData: " + e.getMessage());
        }
    }
}