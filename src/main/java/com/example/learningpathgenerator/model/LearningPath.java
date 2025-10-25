package com.example.learningpathgenerator.model;

import java.util.List;

public class LearningPath {
    private String title;
    private String userName;
    private String skillLevel;
    private int weeklyHours;
    private int estimatedWeeks;
    private int totalHours;
    private List<module> modules;  // Changed from module to Module

    public LearningPath(String title, String userName, String skillLevel, int weeklyHours, int estimatedWeeks, int totalHours, List<module> modules) {
        this.title = title;
        this.userName = userName;
        this.skillLevel = skillLevel;
        this.weeklyHours = weeklyHours;
        this.estimatedWeeks = estimatedWeeks;
        this.totalHours = totalHours;
        this.modules = modules;
    }

    public String getTitle() { return title; }
    public String getUserName() { return userName; }
    public String getSkillLevel() { return skillLevel; }
    public int getWeeklyHours() { return weeklyHours; }
    public int getEstimatedWeeks() { return estimatedWeeks; }
    public int getTotalHours() { return totalHours; }
    public List<module> getmodules() { return modules; }  // Changed return type
}