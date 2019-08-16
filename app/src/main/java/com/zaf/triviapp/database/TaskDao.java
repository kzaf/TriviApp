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
    LiveData<UserDetails> loadUserDetails();

    @Insert
    void insertLoggedUser(UserDetails user);

    @Query("DELETE FROM user_details")
    void deleteUser();


    // Scores
    @Query("SELECT * FROM category_scores")
    LiveData<Scores[]> loadAllCategoriesScore();

    @Query("SELECT * FROM category_scores WHERE category_name = :category")
    LiveData<Scores> loadSelectedCategoryScore(String category);

    @Insert
    void updateScore(Scores score);

    @Delete
    void resetScore(Scores score);

}
