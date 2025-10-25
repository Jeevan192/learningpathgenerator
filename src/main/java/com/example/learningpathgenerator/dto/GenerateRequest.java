package com.example.learningpathgenerator.dto;

import java.util.List;

public class GenerateRequest {
    private String name;
    private String skillLevel;
    private List<String> interests;
    private int weeklyHours;
    private String target;

    public GenerateRequest() {}

    public String getName() { return name; }
    public String getSkillLevel() { return skillLevel; }
    public List<String> getInterests() { return interests; }
    public int getWeeklyHours() { return weeklyHours; }
    public String getTarget() { return target; }

    public void setName(String name) { this.name = name; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public void setWeeklyHours(int weeklyHours) { this.weeklyHours = weeklyHours; }
    public void setTarget(String target) { this.target = target; }
}