package com.example.hireready;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserSession {

    private String sessionId;        // Firestore document ID
    private String packTitle;
    private List<String> answers;    // Firestore field: "answers"
    private Map<String, Object> aiFeedback; // Firestore field: "aiFeedback"
    private int score;               // Firestore field: "score"
    private Date timeStamp;

    public UserSession() {}

    // ===== GETTERS & SETTERS =====

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPackTitle() {
        return packTitle;
    }

    public void setPackTitle(String packTitle) {
        this.packTitle = packTitle;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public Map<String, Object> getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(Map<String, Object> aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @ServerTimestamp
    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
