package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.dto.LearningPathSaveRequest;
import com.example.learningpathgenerator.model.LearningPath;
import com.example.learningpathgenerator.Service.UserLearningPathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/learning-path")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class UserLearningPathController {

    private final UserLearningPathService userLearningPathService;

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserLearningPath(@PathVariable String username) {
        log.info("GET request for learning path of user: {}", username);

        return userLearningPathService.getUserLearningPath(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveLearningPath(@RequestBody LearningPathSaveRequest request) {
        log.info("POST request to save learning path for user: {}", request.getUsername());

        try {
            userLearningPathService.saveLearningPath(request.getUsername(), request.getLearningPath());
            return ResponseEntity.ok(Map.of("message", "Learning path saved successfully"));
        } catch (Exception e) {
            log.error("Error saving learning path", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save learning path: " + e.getMessage()));
        }
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<?> deleteLearningPath(@PathVariable String username) {
        log.info("DELETE request for learning path of user: {}", username);

        try {
            userLearningPathService.deleteLearningPath(username);
            return ResponseEntity.ok(Map.of("message", "Learning path deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting learning path", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete learning path: " + e.getMessage()));
        }
    }
}