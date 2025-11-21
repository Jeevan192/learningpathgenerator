package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.entity.*;
import com.example.learningpathgenerator.dto.*;
import com.example.learningpathgenerator.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final LearningResourceRepository resourceRepository;
    private final ResourceProgressRepository resourceProgressRepository;
    private final MLModelService mlModelService;
    private final AIContentService aiContentService;
    private final QuizAnalysisService quizAnalysisService;

    @Transactional
    public LearningPath generateLearningPath(User user, QuizAttempt quizAttempt) {
        log.info("Generating learning path for user: {}", user.getId());

        // Analyze quiz to get skill profile
        QuizAnalysisResult analysis = quizAnalysisService.analyzeQuizAttempt(quizAttempt);
        SkillProfile skillProfile = analysis.getSkillProfile();

        // Create learning path
        LearningPath path = new LearningPath();
        path.setUser(user);
        path.setTargetSkill(quizAttempt.getQuiz().getTopic());
        path.setStatus("ACTIVE");
        path.setCreatedAt(LocalDateTime.now());
        path.setCompletionPercentage(0.0);

        // Determine difficulty level
        String difficulty = determineDifficultyLevel(skillProfile);
        path.setDifficultyLevel(difficulty);

        // Get available resources
        List<LearningResource> availableResources = resourceRepository.findAll();

        // Use ML to recommend resources
        List<ResourceRecommendation> recommendations = mlModelService.recommendResources(
                skillProfile,
                availableResources,
                10  // top 10 resources
        );

        // Create ordered learning resources
        List<LearningResource> pathResources = new ArrayList<>();
        int sequence = 1;
        int totalDuration = 0;

        for (ResourceRecommendation rec : recommendations) {
            LearningResource resource = new LearningResource();
            resource.setLearningPath(path);
            resource.setTitle(rec.getResource().getTitle());
            resource.setDescription(rec.getResource().getDescription());
            resource.setResourceType(rec.getResource().getResourceType());
            resource.setUrl(rec.getResource().getUrl());
            resource.setProvider(rec.getResource().getProvider());
            resource.setSequenceNumber(sequence++);
            resource.setEstimatedDuration(rec.getResource().getEstimatedDuration());
            resource.setTags(rec.getResource().getTags());
            resource.setDifficultyLevel(rec.getResource().getDifficultyLevel());
            resource.setRelevanceScore(rec.getRelevanceScore());

            pathResources.add(resource);
            totalDuration += rec.getResource().getEstimatedDuration() != null ?
                    rec.getResource().getEstimatedDuration() : 0;
        }

        path.setResources(pathResources);
        path.setEstimatedDuration(totalDuration / 60); // convert to hours

        // Generate AI-powered description
        try {
            String description = aiContentService.generateLearningPathDescription(
                    path.getTargetSkill(),
                    skillProfile.getWeaknesses() != null ? skillProfile.getWeaknesses() : List.of(),
                    skillProfile.getStrengths() != null ? skillProfile.getStrengths() : List.of(),
                    skillProfile.getRecommendedLearningStyle()
            );
            path.setDescription(description);
        } catch (Exception e) {
            log.error("Failed to generate AI description", e);
            path.setDescription("A personalized learning path to master " + path.getTargetSkill());
        }

        // Set title
        path.setTitle(String.format("Master %s - Personalized Path", path.getTargetSkill()));

        // Generate explanation
        path.setGenerationReason(generatePathReason(skillProfile, recommendations));

        return learningPathRepository.save(path);
    }

    @Transactional
    public void updateProgress(Long pathId, Long resourceId, int progressPercentage) {
        LearningPath path = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Path not found"));

        User user = path.getUser();

        // Find or create resource progress
        LearningResource resource = path.getResources().stream()
                .filter(r -> r.getId().equals(resourceId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        ResourceProgress progress = resourceProgressRepository
                .findByUserAndResource(user, resource)
                .orElseGet(() -> {
                    ResourceProgress newProgress = new ResourceProgress();
                    newProgress.setUser(user);
                    newProgress.setResource(resource);
                    newProgress.setStartedAt(LocalDateTime.now());
                    newProgress.setStatus("IN_PROGRESS");
                    return newProgress;
                });

        progress.setProgressPercentage(progressPercentage);

        if (progressPercentage >= 100) {
            progress.setStatus("COMPLETED");
            progress.setCompletedAt(LocalDateTime.now());
        }

        resourceProgressRepository.save(progress);

        // Calculate overall path completion
        long completedResources = path.getResources().stream()
                .filter(r -> {
                    Optional<ResourceProgress> rp = resourceProgressRepository.findByUserAndResource(user, r);
                    return rp.isPresent() && "COMPLETED".equals(rp.get().getStatus());
                })
                .count();

        double totalProgress = (double) completedResources / path.getResources().size() * 100;
        path.setCompletionPercentage(totalProgress);
        path.setLastAccessedAt(LocalDateTime.now());

        if (totalProgress >= 100.0) {
            path.setStatus("COMPLETED");
        }

        learningPathRepository.save(path);
    }

    public LearningResource getNextResource(Long pathId, User user) {
        LearningPath path = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Path not found"));

        // Find first incomplete resource
        return path.getResources().stream()
                .filter(r -> {
                    Optional<ResourceProgress> rp = resourceProgressRepository.findByUserAndResource(user, r);
                    return rp.isEmpty() || !"COMPLETED".equals(rp.get().getStatus());
                })
                .findFirst()
                .orElse(null);
    }

    public List<LearningPath> getUserPaths(User user) {
        return learningPathRepository.findByUser(user);
    }

    public List<LearningPath> getActivePaths(User user) {
        return learningPathRepository.findByUserAndStatus(user, "ACTIVE");
    }

    private String determineDifficultyLevel(SkillProfile profile) {
        if (profile.getCurrentLevel() != null) {
            return profile.getCurrentLevel();
        }

        Map<String, String> proficiency = profile.getSkillProficiency();
        if (proficiency == null || proficiency.isEmpty()) {
            return "BEGINNER";
        }

        long advancedCount = proficiency.values().stream()
                .filter(level -> "ADVANCED".equals(level))
                .count();
        long intermediateCount = proficiency.values().stream()
                .filter(level -> "INTERMEDIATE".equals(level))
                .count();

        if (advancedCount > proficiency.size() / 2) return "ADVANCED";
        if (intermediateCount > proficiency.size() / 2) return "INTERMEDIATE";
        return "BEGINNER";
    }

    private String generatePathReason(SkillProfile profile,
                                      List<ResourceRecommendation> recommendations) {
        StringBuilder reason = new StringBuilder();
        reason.append("This path was generated based on your quiz performance. ");

        if (profile.getWeaknesses() != null && !profile.getWeaknesses().isEmpty()) {
            reason.append("Focuses on improving: ")
                    .append(String.join(", ", profile.getWeaknesses()))
                    .append(". ");
        }

        reason.append("Resources are ordered to build skills progressively. ");

        int totalMinutes = recommendations.stream()
                .filter(r -> r.getResource().getEstimatedDuration() != null)
                .mapToInt(r -> r.getResource().getEstimatedDuration())
                .sum();

        reason.append("Estimated completion: ")
                .append(totalMinutes / 60)
                .append(" hours.");

        return reason.toString();
    }
}