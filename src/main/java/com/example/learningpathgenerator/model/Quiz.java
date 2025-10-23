package com.example.learningpathgenerator.model;

import java.util.List;

public class Quiz {
    private String topicId;
    private String topicName;
    private List<Question> questions;

    public Quiz() {}

    public Quiz(String topicId, String topicName, List<Question> questions) {
        this.topicId = topicId;
        this.topicName = topicName;
        this.questions = questions;
    }

    public String getTopicId() { return topicId; }
    public String getTopicName() { return topicName; }
    public List<Question> getQuestions() { return questions; }

    public void setTopicId(String topicId) { this.topicId = topicId; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}