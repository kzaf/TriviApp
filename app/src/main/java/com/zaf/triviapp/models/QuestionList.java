package com.zaf.triviapp.models;

import java.util.List;

public class QuestionList {
    public List<Question> results;

    public QuestionList(List<Question> results) {
        this.results = results;
    }

    public List<Question> getTrivia_questions() {
        return results;
    }

    public void setTrivia_questions(List<Question> trivia_questions) {
        this.results = trivia_questions;
    }
}
