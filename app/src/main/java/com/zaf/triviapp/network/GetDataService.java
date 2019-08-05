package com.zaf.triviapp.network;

import com.zaf.triviapp.models.CategoriesList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GetDataService {
    @GET("/api_category.php")
    Call<CategoriesList> getAllCategories();
}
