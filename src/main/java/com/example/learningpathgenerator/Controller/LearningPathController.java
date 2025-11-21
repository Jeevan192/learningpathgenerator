package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.entity.*;
import com.example.learningpathgenerator.repository.*;
import com.example.learningpathgenerator.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-path")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LearningPathController {

    private final LearningPathService learningPathService;
    private final UserRepository userRepository;
    private final LearningPathRepository learningPathRepository;
    private final GamificationService gamificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LearningPath>> getUserPaths(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(learningPathService.getUserPaths(user));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<LearningPath>> getActivePaths(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(learningPathService.getActivePaths(user));
    }

    @GetMapping("/{pathId}")
    public ResponseEntity<LearningPath> getPath(@PathVariable Long pathId) {
        return learningPathRepository.findById(pathId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{pathId}/next-resource")
    public ResponseEntity<LearningResource> getNextResource(
            @PathVariable Long pathId,
            @RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LearningResource resource = learningPathService.getNextResource(pathId, user);

        if (resource == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{pathId}/resource/{resourceId}/progress")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @PathVariable Long pathId,
            @PathVariable Long resourceId,
            @RequestParam int progressPercentage,
            @RequestParam Long userId) {

        learningPathService.updateProgress(pathId, resourceId, progressPercentage);

        // If completed (100%), award gamification points
        if (progressPercentage >= 100) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LearningPath path = learningPathRepository.findById(pathId)
                    .orElseThrow(() -> new RuntimeException("Path not found"));

            LearningResource resource = path.getResources().stream()
                    .filter(r -> r.getId().equals(resourceId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Resource not found"));

            Map<String, Object> rewards = gamificationService.awardResourcePoints(user, resource);

            // Check if entire path is completed
            if (path.getCompletionPercentage() >= 100.0) {
                Map<String, Object> pathRewards = gamificationService.awardPathCompletionPoints(user, path);
                rewards.put("pathCompletion", pathRewards);
            }

            return ResponseEntity.ok(rewards);
        }

        return ResponseEntity.ok(Map.of("success", true));
    }
}