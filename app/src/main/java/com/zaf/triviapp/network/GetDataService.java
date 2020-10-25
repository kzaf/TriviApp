package com.zaf.triviapp.network;

import com.zaf.triviapp.models.CategoriesList;
import com.zaf.triviapp.models.QuestionList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetDataService {

    @GET("/api_category.php")
    Call<CategoriesList> getAllCategories();

    @GET("/api.php?amount=10")
    Call<QuestionList> getQuestions(@Query("category") int category,
                                    @Query("difficulty") String difficulty,
                                    @Query("type") String type);
}
