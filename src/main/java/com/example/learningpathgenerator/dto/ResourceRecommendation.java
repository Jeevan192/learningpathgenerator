package com.example.learningpathgenerator.dto;

import com.example.learningpathgenerator.entity.LearningResource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRecommendation {
    private LearningResource resource;
    private Double relevanceScore;
    private String reason;
}