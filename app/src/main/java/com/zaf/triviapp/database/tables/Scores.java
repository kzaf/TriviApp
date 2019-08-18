package com.zaf.triviapp.database.tables;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "category_scores")
public class Scores implements Parcelable {
    @ColumnInfo(name = "scores_uid")
    private String userId;
    @PrimaryKey
    @ColumnInfo(name = "category_name")
    private @NotNull String categoryName;
    @ColumnInfo(name = "category_score")
    private int categoryScore;

    public Scores(@NotNull String userId, String categoryName, int categoryScore) {
        this.userId = userId;
        this.categoryName = categoryName;
        this.categoryScore = categoryScore;
    }

    protected Scores(Parcel in) {
        userId = in.readString();
        categoryName = in.readString();
        categoryScore = in.readInt();
    }

    public static final Creator<Scores> CREATOR = new Creator<Scores>() {
        @Override
        public Scores createFromParcel(Parcel in) {
            return new Scores(in);
        }

        @Override
        public Scores[] newArray(int size) {
            return new Scores[size];
        }
    };

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

    public int getCategoryScore() {
        return categoryScore;
    }

    public void setCategoryScore(int categoryScore) {
        this.categoryScore = categoryScore;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(categoryName);
        dest.writeInt(categoryScore);
    }
}
