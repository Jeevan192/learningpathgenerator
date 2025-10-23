package com.example.learningpathgenerator.model;

import java.util.List;

public class module {
    private String title;
    private String description;
    private List<String> resources;
    private int hours;

    public module(String title, String description, List<String> resources, int hours) {
        this.title = title;
        this.description = description;
        this.resources = resources;
        this.hours = hours;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public java.util.List<String> getResources() {
        return resources;
    }

    public int getHours() {
        return hours;
    }
}