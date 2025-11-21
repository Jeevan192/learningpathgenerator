package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.dto.SkillProfile;
import com.example.learningpathgenerator.dto.ResourceRecommendation;
import com.example.learningpathgenerator.entity.LearningResource;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MLModelService {

    public SkillProfile analyzeQuizResults(Map<String, Double> skillScores,
                                           List<Map<String, Double>> historicalScores) {
        SkillProfile profile = new SkillProfile();

        // Calculate proficiency levels
        Map<String, String> proficiencyLevels = new HashMap<>();
        for (Map.Entry<String, Double> entry : skillScores.entrySet()) {
            String skill = entry.getKey();
            Double score = entry.getValue();

            String level;
            if (score >= 0.8) level = "ADVANCED";
            else if (score >= 0.6) level = "INTERMEDIATE";
            else level = "BEGINNER";

            proficiencyLevels.put(skill, level);
        }

        profile.setSkillProficiency(proficiencyLevels);

        // Identify strengths and weaknesses
        List<String> strengths = skillScores.entrySet().stream()
                .filter(e -> e.getValue() >= 0.7)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> weaknesses = skillScores.entrySet().stream()
                .filter(e -> e.getValue() < 0.5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        profile.setStrengths(strengths);
        profile.setWeaknesses(weaknesses);

        // Calculate learning velocity
        if (!historicalScores.isEmpty()) {
            double velocity = calculateLearningVelocity(historicalScores);
            profile.setLearningVelocity(velocity);
        }

        // Recommend learning style
        profile.setRecommendedLearningStyle(predictLearningStyle(skillScores, historicalScores));

        // Determine current level
        double avgProficiency = skillScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        if (avgProficiency >= 0.8) profile.setCurrentLevel("ADVANCED");
        else if (avgProficiency >= 0.6) profile.setCurrentLevel("INTERMEDIATE");
        else profile.setCurrentLevel("BEGINNER");

        return profile;
    }

    public List<ResourceRecommendation> recommendResources(
            SkillProfile userProfile,
            List<LearningResource> availableResources,
            int topN) {

        List<ResourceRecommendation> recommendations = new ArrayList<>();

        for (LearningResource resource : availableResources) {
            double relevanceScore = calculateRelevanceScore(userProfile, resource);

            if (relevanceScore > 0.5) {
                ResourceRecommendation rec = new ResourceRecommendation();
                rec.setResource(resource);
                rec.setRelevanceScore(relevanceScore);
                rec.setReason(generateRecommendationReason(userProfile, resource));
                recommendations.add(rec);
            }
        }

        return recommendations.stream()
                .sorted(Comparator.comparingDouble(ResourceRecommendation::getRelevanceScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    private double calculateRelevanceScore(SkillProfile profile, LearningResource resource) {
        double score = 0.0;

        // Factor 1: Skill gap alignment (40%)
        double gapAlignment = 0.0;
        for (String weakness : profile.getWeaknesses()) {
            if (resource.getTags().contains(weakness)) {
                gapAlignment += 1.0;
            }
        }
        if (!profile.getWeaknesses().isEmpty()) {
            gapAlignment = gapAlignment / profile.getWeaknesses().size();
        }
        score += gapAlignment * 0.4;

        // Factor 2: Difficulty match (30%)
        String userLevel = profile.getCurrentLevel();
        String resourceLevel = resource.getDifficultyLevel();
        double difficultyMatch = calculateDifficultyMatch(userLevel, resourceLevel);
        score += difficultyMatch * 0.3;

        // Factor 3: Learning style match (20%)
        double styleMatch = calculateStyleMatch(profile.getRecommendedLearningStyle(),
                resource.getResourceType());
        score += styleMatch * 0.2;

        // Factor 4: Time commitment match (10%)
        double timeMatch = 0.7; // Default if time not specified
        if (profile.getAvailableTimePerWeek() != null && resource.getEstimatedDuration() != null) {
            timeMatch = calculateTimeMatch(profile.getAvailableTimePerWeek(),
                    resource.getEstimatedDuration());
        }
        score += timeMatch * 0.1;

        return Math.min(score, 1.0);
    }

    private double calculateDifficultyMatch(String userLevel, String resourceLevel) {
        if (userLevel == null || resourceLevel == null) return 0.5;

        Map<String, Integer> levelMap = Map.of(
                "BEGINNER", 1,
                "INTERMEDIATE", 2,
                "ADVANCED", 3
        );

        int userLevelNum = levelMap.getOrDefault(userLevel, 1);
        int resourceLevelNum = levelMap.getOrDefault(resourceLevel, 1);

        int diff = Math.abs(userLevelNum - resourceLevelNum);
        if (diff == 0) return 1.0;
        if (diff == 1 && resourceLevelNum > userLevelNum) return 0.8;
        if (diff == 1) return 0.6;
        return 0.3;
    }

    private double calculateStyleMatch(String learningStyle, String resourceType) {
        if (learningStyle == null || resourceType == null) return 0.5;

        Map<String, List<String>> stylePreferences = Map.of(
                "VISUAL", List.of("VIDEO", "TUTORIAL"),
                "READING", List.of("ARTICLE", "DOCUMENTATION"),
                "HANDS_ON", List.of("EXERCISE", "PROJECT"),
                "MIXED", List.of("VIDEO", "ARTICLE", "EXERCISE")
        );

        List<String> preferred = stylePreferences.getOrDefault(learningStyle, Collections.emptyList());
        return preferred.contains(resourceType) ? 1.0 : 0.5;
    }

    private double calculateTimeMatch(Integer availableTime, Integer resourceDuration) {
        double ratio = (double) resourceDuration / availableTime;
        if (ratio <= 1.0) return 1.0;
        if (ratio <= 1.5) return 0.8;
        if (ratio <= 2.0) return 0.6;
        return 0.3;
    }

    private double calculateLearningVelocity(List<Map<String, Double>> historicalScores) {
        if (historicalScores.size() < 2) return 0.5;

        double totalImprovement = 0.0;
        for (int i = 1; i < historicalScores.size(); i++) {
            Map<String, Double> prev = historicalScores.get(i - 1);
            Map<String, Double> curr = historicalScores.get(i);

            for (String skill : curr.keySet()) {
                if (prev.containsKey(skill)) {
                    double improvement = curr.get(skill) - prev.get(skill);
                    totalImprovement += improvement;
                }
            }
        }

        double avgImprovement = totalImprovement / (historicalScores.size() - 1);
        return Math.max(0, Math.min(1, 0.5 + avgImprovement * 2));
    }

    private String predictLearningStyle(Map<String, Double> currentScores,
                                        List<Map<String, Double>> historical) {
        double avgScore = currentScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.5);

        if (avgScore >= 0.7) return "HANDS_ON";
        if (avgScore >= 0.5) return "MIXED";
        return "VISUAL";
    }

    private String generateRecommendationReason(SkillProfile profile, LearningResource resource) {
        StringBuilder reason = new StringBuilder();

        for (String weakness : profile.getWeaknesses()) {
            if (resource.getTags().contains(weakness)) {
                reason.append("Addresses skill gap in ").append(weakness).append(". ");
                break;
            }
        }

        if (resource.getDifficultyLevel() != null && resource.getDifficultyLevel().equals(profile.getCurrentLevel())) {
            reason.append("Matches your current level. ");
        }

        return reason.toString().trim();
    }
}