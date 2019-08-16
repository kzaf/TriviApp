package com.zaf.triviapp.database.tables;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "user_details")
public class UserDetails {
    @PrimaryKey
    @ColumnInfo(name = "user_uid")
    private String userId;
    @ColumnInfo(name = "user_name")
    private String userName;
    @ColumnInfo(name = "user_email")
    private String userEmail;
    @ColumnInfo(name = "user_scores")
    private String userScrores;
}
