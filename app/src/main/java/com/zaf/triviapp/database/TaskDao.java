package com.zaf.triviapp.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.database.tables.UserDetails;

@Dao
public interface TaskDao {

    // User
    @Query("SELECT * FROM user_details")
    LiveData<UserDetails[]> LoadUserDetails();

    @Insert
    void InsertLoggedUser(UserDetails user);

    @Delete
    void DeleteUser(UserDetails user);


    // Scores
    @Query("SELECT * FROM category_scores")
    LiveData<Scores[]> LoadAllCategoriesScore();

    @Query("SELECT * FROM category_scores WHERE category_name = :category")
    LiveData<Scores> LoadSelectedCategoryScore(String category);

    @Insert
    void UpdateScore(Scores score);

    @Delete
    void ResetScore(Scores score);

}
