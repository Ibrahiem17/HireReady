package com.example.hireready;

import com.google.firebase.firestore.Exclude;

import java.util.List;
import java.util.Map;

public class LearningSection {
    private String title;
    private List<String> skills;
    private List<String> interview_tips;
    private List<String> common_questions;
    private Map<String, String> answers;

    @Exclude
    private String documentId;

    public LearningSection() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getInterview_tips() {
        return interview_tips;
    }

    public void setInterview_tips(List<String> interview_tips) {
        this.interview_tips = interview_tips;
    }

    public List<String> getCommon_questions() {
        return common_questions;
    }

    public void setCommon_questions(List<String> common_questions) {
        this.common_questions = common_questions;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
