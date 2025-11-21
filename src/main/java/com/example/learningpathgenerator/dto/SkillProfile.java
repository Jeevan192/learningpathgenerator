package com.example.learningpathgenerator.dto;

import java.util.List;
import java.util.Map;

public class SkillProfile {
    private String currentLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    private Map<String, String> skillProficiency; // skill -> proficiency level
    private List<String> strengths;
    private List<String> weaknesses;
    private String recommendedLearningStyle;

    public SkillProfile() {}

    // Getters and Setters
    public String getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(String currentLevel) {
        this.currentLevel = currentLevel;
    }

    public Map<String, String> getSkillProficiency() {
        return skillProficiency;
    }

    public void setSkillProficiency(Map<String, String> skillProficiency) {
        this.skillProficiency = skillProficiency;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(List<String> weaknesses) {
        this.weaknesses = weaknesses;
    }

    public String getRecommendedLearningStyle() {
        return recommendedLearningStyle;
    }

    public void setRecommendedLearningStyle(String recommendedLearningStyle) {
        this.recommendedLearningStyle = recommendedLearningStyle;
    }
}
