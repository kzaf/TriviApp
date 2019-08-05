package com.zaf.triviapp.models;

import java.util.List;

public class CategoriesList {
    public List<Category> trivia_categories;

    public CategoriesList(List<Category> category) {
        this.trivia_categories = category;
    }

    public List<Category> getCategory() {
        return trivia_categories;
    }

    public void setCategory(List<Category> category) {
        this.trivia_categories = category;
    }
}
