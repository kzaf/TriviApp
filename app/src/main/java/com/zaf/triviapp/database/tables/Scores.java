package com.zaf.triviapp.database.tables;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "category_scores")
public class Scores {
    @PrimaryKey
    @ColumnInfo(name = "scores_uid")
    private String userId;
    @ColumnInfo(name = "category_name")
    private String categoryName;
    @ColumnInfo(name = "category_score")
    private String categoryScore;
}
