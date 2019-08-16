package com.zaf.triviapp.database.tables;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "category_scores")
public class Scores {
    @PrimaryKey
    @NotNull
    @ColumnInfo(name = "scores_uid")
    private String userId;
    @ColumnInfo(name = "category_name")
    private String categoryName;
    @ColumnInfo(name = "category_score")
    private String categoryScore;

    public Scores(@NotNull String userId, String categoryName, String categoryScore) {
        this.userId = userId;
        this.categoryName = categoryName;
        this.categoryScore = categoryScore;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryScore() {
        return categoryScore;
    }

    public void setCategoryScore(String categoryScore) {
        this.categoryScore = categoryScore;
    }
}
