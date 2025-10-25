package com.example.learningpathgenerator.model;

import java.util.List;

public class Question {
    private String id;
    private String text;
    private List<String> options;
    private int correctIndex;

    public Question() {}

    public Question(String id, String text, List<String> options, int correctIndex) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }

    public void setId(String id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }
}