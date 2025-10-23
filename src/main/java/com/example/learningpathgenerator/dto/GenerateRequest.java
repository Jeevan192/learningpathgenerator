package com.example.learningpathgenerator.dto;

import java.util.List;

public class GenerateRequest {
    private String name;
    private String skillLevel; // "BEGINNER", "INTERMEDIATE", "ADVANCED"
    private List<String> interests; // e.g., ["web", "algorithms"]
    private int weeklyHours = 5;
    private String target; // optional target, e.g., "backend developer"

    public GenerateRequest() {
    }

    public String getName() {
        return name;
    }

    public GenerateRequest setName(String name) {
        this.name = name;
        return this;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public GenerateRequest setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
        return this;
    }

    public java.util.List<String> getInterests() {
        return interests;
    }

    public GenerateRequest setInterests(java.util.List<String> interests) {
        this.interests = interests;
        return this;
    }

    public int getWeeklyHours() {
        return weeklyHours;
    }

    public GenerateRequest setWeeklyHours(int weeklyHours) {
        this.weeklyHours = weeklyHours;
        return this;
    }

    public String getTarget() {
        return target;
    }

    public GenerateRequest setTarget(String target) {
        this.target = target;
        return this;
    }
}