package com.zaf.triviapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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
    LiveData<Scores[]> loadAllCategoriesScore();

    @Query("SELECT * FROM category_scores WHERE category_name = :category")
    LiveData<Scores> loadSelectedCategoryScore(String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertScore(Scores score);

    @Query("DELETE FROM category_scores")
    void resetScore();

}
