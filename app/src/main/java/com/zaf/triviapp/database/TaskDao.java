package com.zaf.triviapp.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.database.tables.UserDetails;

import java.util.List;

@Dao
public interface TaskDao {

    // User
    @Query("SELECT * FROM user_details")
    UserDetails loadUserDetails();

    @Query("SELECT * FROM user_details")
    List<UserDetails> checkIfUsersTableIsEmpty();

    @Insert
    void insertLoggedUser(UserDetails user);

    @Query("DELETE FROM user_details")
    void deleteUser();

    // Scores
    @Query("SELECT * FROM category_scores")
    Scores[] loadAllCategoriesScore();

    @Query("SELECT * FROM category_scores WHERE category_name = :category")
    LiveData<Scores> loadSelectedCategoryScore(String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertScore(Scores score);

    @Query("DELETE FROM category_scores")
    void resetScore();

}
