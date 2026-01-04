package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.entity.*;
import com.example.learningpathgenerator.repository.*;
import com.example.learningpathgenerator.Service.*;
import com.example.learningpathgenerator.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/learning-paths")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class LearningPathController {

    private final LearningPathRepository learningPathRepository;
    private final LearningResourceRepository learningResourceRepository;
    private final UserRepository userRepository;
    private final AIContentService aiContentService;
    private final GamificationService gamificationService;

    @GetMapping
    public ResponseEntity<List<LearningPath>> getUserPaths() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<LearningPath> paths = learningPathRepository.findByUserId(Long.valueOf(user.getId()));
        return ResponseEntity.ok(paths);
    }

    @GetMapping("/{pathId}")
    public ResponseEntity<LearningPath> getPathById(@PathVariable Long pathId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<LearningPath> path = learningPathRepository.findById(pathId);

        if (path.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!path.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(path.get());
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateLearningPath(@RequestBody Map<String, Object> request) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        String topic = (String) request.get("topic");
        String skillLevel = (String) request.getOrDefault("skillLevel", "BEGINNER");
        Integer targetDays = (Integer) request.getOrDefault("targetDays", 30);

        @SuppressWarnings("unchecked")
        List<String> weaknesses = (List<String>) request.getOrDefault("weaknesses", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> strengths = (List<String>) request.getOrDefault("strengths", new ArrayList<>());

        log.info("Generating learning path for topic: {}, skillLevel: {}, user: {}", topic, skillLevel, user.getUsername());

        try {
            // Generate AI-based learning resources
            String aiResponse = aiContentService.generateLearningPathJson(topic, weaknesses);

            // Create learning path
            LearningPath path = new LearningPath();
            path.setUser(user);
            path.setTitle("Master " + topic + " - Personalized Path");
            path.setTargetSkill(topic);
            path.setDifficultyLevel(skillLevel);
            path.setStatus("ACTIVE");
            path.setCreatedAt(LocalDateTime.now());
            path.setCompletionPercentage(0.0);
            path.setEstimatedDuration(targetDays * 60); // minutes

            // Generate AI description
            String description = aiContentService.generateLearningPathDescription(
                    topic, weaknesses, strengths, skillLevel
            );
            path.setDescription(description);
            path.setGenerationReason("AI-generated based on your skill assessment and learning goals.");

            // Save path first
            LearningPath savedPath = learningPathRepository.save(path);

            // Parse AI response and create resources using topological sort
            List<LearningResource> resources = parseAndOrderResources(aiResponse, savedPath, skillLevel);
            savedPath.setResources(resources);

            // Save the final path with resources
            savedPath = learningPathRepository.save(savedPath);

            log.info("Learning path created with {} resources", resources.size());
            return ResponseEntity.ok(savedPath);

        } catch (Exception e) {
            log.error("Error generating learning path: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to generate learning path: " + e.getMessage()));
        }
    }

    @PostMapping("/from-quiz")
    public ResponseEntity<?> generateFromQuizAnalysis(@RequestBody QuizAnalysisResult quizResult) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        SkillProfile profile = quizResult.getSkillProfile();
        if (profile == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No skill profile in quiz result"));
        }

        log.info("Generating learning path from quiz analysis for user: {}", user.getUsername());

        try {
            String topic = profile.getSkillProficiency() != null ?
                    profile.getSkillProficiency().keySet().stream().findFirst().orElse("General") : "General";

            List<String> weaknesses = profile.getWeaknesses() != null ? profile.getWeaknesses() : new ArrayList<>();
            List<String> strengths = profile.getStrengths() != null ? profile.getStrengths() : new ArrayList<>();

            String aiResponse = aiContentService.generateLearningPathJson(topic, weaknesses);

            LearningPath path = new LearningPath();
            path.setUser(user);
            path.setTitle("Personalized Path - Based on Your Assessment");
            path.setTargetSkill(topic);
            path.setDifficultyLevel(profile.getCurrentLevel() != null ? profile.getCurrentLevel() : "INTERMEDIATE");
            path.setStatus("ACTIVE");
            path.setCreatedAt(LocalDateTime.now());
            path.setCompletionPercentage(0.0);

            String description = aiContentService.generateLearningPathDescription(
                    topic, weaknesses, strengths, profile.getRecommendedLearningStyle()
            );
            path.setDescription(description);
            path.setGenerationReason(String.format(
                    "Generated based on quiz performance (%.1f%%). Focus areas: %s",
                    quizResult.getScorePercentage(),
                    String.join(", ", weaknesses)
            ));

            LearningPath savedPath = learningPathRepository.save(path);

            List<LearningResource> resources = parseAndOrderResources(aiResponse, savedPath, path.getDifficultyLevel());
            savedPath.setResources(resources);
            savedPath = learningPathRepository.save(savedPath);

            return ResponseEntity.ok(savedPath);

        } catch (Exception e) {
            log.error("Error generating path from quiz: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to generate learning path"));
        }
    }

    @PutMapping("/{pathId}/resources/{resourceId}/progress")
    public ResponseEntity<?> updateResourceProgress(
            @PathVariable Long pathId,
            @PathVariable Long resourceId,
            @RequestBody Map<String, Object> body) {

        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<LearningPath> pathOpt = learningPathRepository.findById(pathId);
        if (pathOpt.isEmpty() || !pathOpt.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        LearningPath path = pathOpt.get();

        // Mark resource as completed
        path.getResources().stream()
                .filter(r -> r.getId().equals(resourceId))
                .findFirst()
                .ifPresent(resource -> {
                    // Award points for completing resource
                    gamificationService.awardResourcePoints(user, resource);
                });

        // Calculate completion percentage
        long completedResources = path.getResources().stream()
                .filter(r -> r.getId() <= resourceId) // Simple progress tracking
                .count();

        double completionPercentage = path.getResources().isEmpty() ? 0 :
                (completedResources / (double) path.getResources().size()) * 100;

        path.setCompletionPercentage(completionPercentage);
        path.setLastAccessedAt(LocalDateTime.now());

        // Check if path is completed
        if (completionPercentage >= 100) {
            path.setStatus("COMPLETED");
            gamificationService.awardPathCompletionPoints(user, path);
        }

        learningPathRepository.save(path);

        return ResponseEntity.ok(Map.of(
                "message", "Progress updated",
                "completion", completionPercentage,
                "status", path.getStatus()
        ));
    }

    @DeleteMapping("/{pathId}")
    public ResponseEntity<?> deletePath(@PathVariable Long pathId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<LearningPath> pathOpt = learningPathRepository.findById(pathId);
        if (pathOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!pathOpt.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        learningPathRepository.deleteById(pathId);
        return ResponseEntity.ok(Map.of("message", "Learning path deleted"));
    }

    @GetMapping("/export/{pathId}")
    public void exportCsv(@PathVariable Long pathId, HttpServletResponse response) throws IOException {
        User user = getCurrentUser();
        if (user == null) {
            response.sendError(401, "Unauthorized");
            return;
        }

        Optional<LearningPath> pathOpt = learningPathRepository.findById(pathId);
        if (pathOpt.isEmpty()) {
            response.sendError(404, "Learning path not found");
            return;
        }

        LearningPath path = pathOpt.get();

        if (!path.getUser().getId().equals(user.getId())) {
            response.sendError(403, "Access denied");
            return;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"learning_path_" + pathId + ".csv\"");

        PrintWriter writer = response.getWriter();

        // Write header information
        writer.println("# Learning Path: " + escapeCSV(path.getTitle()));
        writer.println("# Target Skill: " + escapeCSV(path.getTargetSkill()));
        writer.println("# Difficulty: " + escapeCSV(path.getDifficultyLevel()));
        writer.println("# Status: " + path.getStatus());
        writer.println("# Completion: " + String.format("%.1f%%", path.getCompletionPercentage()));
        writer.println("# Created: " + path.getCreatedAt());
        writer.println("#");
        writer.println("Order,Title,Description,Type,URL,Duration (mins),Difficulty,Prerequisites");

        if (path.getResources() != null) {
            int order = 1;
            for (LearningResource resource : path.getResources()) {
                writer.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        order++,
                        escapeCSV(resource.getTitle()),
                        escapeCSV(resource.getDescription()),
                        resource.getResourceType() != null ? resource.getResourceType() : "RESOURCE",
                        resource.getUrl() != null ? resource.getUrl() : "",
                        resource.getEstimatedDuration() != null ? resource.getEstimatedDuration() : 0,
                        resource.getDifficultyLevel() != null ? resource.getDifficultyLevel() : "N/A",
                        resource.getTags() != null ? String.join("; ", resource.getTags()) : ""
                );
            }
        }

        writer.flush();
    }

    /**
     * Parse AI response and order resources using topological sort based on difficulty
     */
    private List<LearningResource> parseAndOrderResources(String aiResponse, LearningPath path, String userLevel) {
        List<LearningResource> resources = new ArrayList<>();

        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            log.warn("Empty AI response, using default resources");
            return createDefaultResources(path);
        }

        try {
            log.info("Parsing AI response: {}", aiResponse.substring(0, Math.min(100, aiResponse.length())) + "...");

            // Parse JSON response
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

            // Clean the response if it contains markdown
            String cleanedResponse = aiResponse.trim();
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            } else if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();

            List<Map<String, Object>> resourcesData = mapper.readValue(
                    cleanedResponse,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );

            log.info("Parsed {} resources from AI response", resourcesData.size());

            int sequence = 1;
            for (Map<String, Object> resData : resourcesData) {
                LearningResource resource = new LearningResource();
                resource.setLearningPath(path);
                resource.setTitle((String) resData.getOrDefault("title", "Learning Resource " + sequence));
                resource.setDescription((String) resData.getOrDefault("description", "Resource description"));
                resource.setUrl((String) resData.getOrDefault("url", "https://example.com"));
                resource.setResourceType((String) resData.getOrDefault("resourceType", "ARTICLE"));
                resource.setEstimatedDuration(resData.get("estimatedDuration") instanceof Number ?
                        ((Number) resData.get("estimatedDuration")).intValue() : 30);
                resource.setDifficultyLevel((String) resData.getOrDefault("difficulty", "INTERMEDIATE"));
                resource.setSequenceNumber(sequence++);

                resources.add(learningResourceRepository.save(resource));
            }

            if (resources.isEmpty()) {
                log.warn("No resources parsed, using defaults");
                return createDefaultResources(path);
            }

            // Apply topological sort based on difficulty
            resources = topologicalSortByDifficulty(resources, userLevel);

            // Update sequence numbers after sort
            for (int i = 0; i < resources.size(); i++) {
                resources.get(i).setSequenceNumber(i + 1);
                learningResourceRepository.save(resources.get(i));
            }

        } catch (Exception e) {
            log.error("Error parsing AI response: {} - Response was: {}", e.getMessage(),
                    aiResponse.substring(0, Math.min(200, aiResponse.length())));
            // Return default resources if parsing fails
            resources = createDefaultResources(path);
        }

        return resources;
    }

    /**
     * Topological sort resources by difficulty level
     */
    private List<LearningResource> topologicalSortByDifficulty(List<LearningResource> resources, String userLevel) {
        Map<String, Integer> difficultyOrder = Map.of(
                "BEGINNER", 1,
                "EASY", 2,
                "INTERMEDIATE", 3,
                "MEDIUM", 3,
                "ADVANCED", 4,
                "HARD", 5
        );

        int userLevelOrder = difficultyOrder.getOrDefault(userLevel.toUpperCase(), 2);

        // Sort by difficulty, starting from user's level
        resources.sort((a, b) -> {
            int diffA = difficultyOrder.getOrDefault(
                    a.getDifficultyLevel() != null ? a.getDifficultyLevel().toUpperCase() : "MEDIUM", 3);
            int diffB = difficultyOrder.getOrDefault(
                    b.getDifficultyLevel() != null ? b.getDifficultyLevel().toUpperCase() : "MEDIUM", 3);

            // Resources at or slightly above user level come first
            int scoreA = Math.abs(diffA - userLevelOrder);
            int scoreB = Math.abs(diffB - userLevelOrder);

            if (scoreA != scoreB) return Integer.compare(scoreA, scoreB);
            return Integer.compare(diffA, diffB);
        });

        return resources;
    }

    private List<LearningResource> createDefaultResources(LearningPath path) {
        List<LearningResource> defaults = new ArrayList<>();

        String[] titles = {"Introduction", "Core Concepts", "Hands-on Practice", "Advanced Topics", "Project Work"};
        String[] types = {"VIDEO", "ARTICLE", "PRACTICE", "ARTICLE", "PROJECT"};
        String[] difficulties = {"BEGINNER", "BEGINNER", "INTERMEDIATE", "ADVANCED", "ADVANCED"};

        for (int i = 0; i < titles.length; i++) {
            LearningResource res = new LearningResource();
            res.setLearningPath(path);
            res.setTitle(titles[i] + " - " + path.getTargetSkill());
            res.setDescription("Learn " + path.getTargetSkill() + " through " + types[i].toLowerCase() + " content.");
            res.setResourceType(types[i]);
            res.setDifficultyLevel(difficulties[i]);
            res.setSequenceNumber(i + 1);
            res.setEstimatedDuration(30 + (i * 15));
            res.setUrl("https://example.com/" + path.getTargetSkill().toLowerCase().replace(" ", "-"));
            defaults.add(learningResourceRepository.save(res));
        }

        return defaults;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
    }
}

