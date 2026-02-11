package com.example.hireready;

import java.util.List;

public class QuestionPack {
    private String role;
    private List<String> questions;


    public QuestionPack() {}

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }
}
