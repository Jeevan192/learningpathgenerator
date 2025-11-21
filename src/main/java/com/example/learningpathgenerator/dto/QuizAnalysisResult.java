package com.example.learningpathgenerator.dto;

public class QuizAnalysisResult {
    private SkillProfile skillProfile;
    private Double overallScore;
    private String performanceLevel; // EXCELLENT, GOOD, AVERAGE, POOR
    private String recommendation;

    public QuizAnalysisResult() {}

    // Getters and Setters
    public SkillProfile getSkillProfile() {
        return skillProfile;
    }

    public void setSkillProfile(SkillProfile skillProfile) {
        this.skillProfile = skillProfile;
    }

    public Double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }

    public String getPerformanceLevel() {
        return performanceLevel;
    }

    public void setPerformanceLevel(String performanceLevel) {
        this.performanceLevel = performanceLevel;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
