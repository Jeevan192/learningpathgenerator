package com.example.learningpathgenerator.model;

import java.util.List;

public class module {
    private String title;
    private String description;
    private int hours;
    private List<String> resources;

    public module() {}

    public module(String title, String description, int hours, List<String> resources) {
        this.title = title;
        this.description = description;
        this.hours = hours;
        this.resources = resources;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }

    public List<String> getResources() { return resources; }
    public void setResources(List<String> resources) { this.resources = resources; }
}