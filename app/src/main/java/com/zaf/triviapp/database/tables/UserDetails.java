package com.zaf.triviapp.database.tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "user_details")
public class UserDetails {
    @PrimaryKey
    @NotNull
    @ColumnInfo(name = "user_uid")
    private String userId;
    @ColumnInfo(name = "user_name")
    private String userName;
    @ColumnInfo(name = "user_email")
    private String userEmail;
    @ColumnInfo(name = "user_scores")
    private int userScores;

    public UserDetails(String userId, String userName, String userEmail, int userScores) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userScores = userScores;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getUserScores() {
        return userScores;
    }

    public void setUserScores(int userScores) {
        this.userScores = userScores;
    }
}
