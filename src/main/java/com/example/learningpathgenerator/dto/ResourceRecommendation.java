package com.example.learningpathgenerator.dto;

import com.example.learningpathgenerator.entity.LearningResource;

public class ResourceRecommendation {
    private LearningResource resource;
    private Double relevanceScore;
    private String reason;

    public ResourceRecommendation() {}

    public ResourceRecommendation(LearningResource resource, Double relevanceScore) {
        this.resource = resource;
        this.relevanceScore = relevanceScore;
    }

    // Getters and Setters
    public LearningResource getResource() {
        return resource;
    }

    public void setResource(LearningResource resource) {
        this.resource = resource;
    }

    public Double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(Double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
