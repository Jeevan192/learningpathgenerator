package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.dto.ResourceRecommendation;
import com.example.learningpathgenerator.dto.SkillProfile;
import com.example.learningpathgenerator.entity.LearningResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MLModelService {

    public List<ResourceRecommendation> recommendResources(
            SkillProfile skillProfile,
            List<LearningResource> availableResources,
            int topN) {

        log.info("Recommending top {} resources for skill profile", topN);

        // Calculate relevance score for each resource
        List<ResourceRecommendation> recommendations = new ArrayList<>();

        for (LearningResource resource : availableResources) {
            double relevanceScore = calculateRelevanceScore(resource, skillProfile);
            recommendations.add(new ResourceRecommendation(resource, relevanceScore));
        }

        // Sort by relevance score and take top N
        return recommendations.stream()
                .sorted(Comparator.comparingDouble(ResourceRecommendation::getRelevanceScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    private double calculateRelevanceScore(LearningResource resource, SkillProfile skillProfile) {
        double score = 0.0;

        // Match difficulty level
        if (resource.getDifficultyLevel() != null &&
                resource.getDifficultyLevel().equals(skillProfile.getCurrentLevel())) {
            score += 0.3;
        }

        // Match weaknesses - prioritize resources that address weaknesses
        if (skillProfile.getWeaknesses() != null && resource.getTags() != null) {
            long matchingWeaknesses = skillProfile.getWeaknesses().stream()
                    .filter(weakness -> resource.getTags().stream()
                            .anyMatch(tag -> tag.toLowerCase().contains(weakness.toLowerCase())))
                    .count();
            score += matchingWeaknesses * 0.4;
        }

        // Match skills
        if (skillProfile.getSkillProficiency() != null && resource.getTags() != null) {
            long matchingSkills = skillProfile.getSkillProficiency().keySet().stream()
                    .filter(skill -> resource.getTags().stream()
                            .anyMatch(tag -> tag.toLowerCase().contains(skill.toLowerCase())))
                    .count();
            score += matchingSkills * 0.2;
        }

        // Prefer certain resource types for hands-on learners
        if ("HANDS_ON".equals(skillProfile.getRecommendedLearningStyle())) {
            if ("EXERCISE".equals(resource.getResourceType()) || 
                "PROJECT".equals(resource.getResourceType())) {
                score += 0.1;
            }
        }

        return Math.min(score, 1.0); // Normalize to 0-1
    }
}
