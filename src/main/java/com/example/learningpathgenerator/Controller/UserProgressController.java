package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.entity.UserProgress;
import com.example.learningpathgenerator.Service.UserProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class UserProgressController {

    private final UserProgressService userProgressService;

    public UserProgressController(UserProgressService userProgressService) {
        this.userProgressService = userProgressService;
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserProgress(@PathVariable String username) {
        return userProgressService.getUserProgress(username)
                .map(progress -> {
                    System.out.println("✅ Retrieved progress for " + username + ": " + progress.getOverallProgress() + "%");
                    return ResponseEntity.ok(progress);
                })
                .orElseGet(() -> {
                    System.out.println("⚠️ No progress found for " + username);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProgress(@RequestBody UserProgress progress) {
        try {
            UserProgress saved = userProgressService.saveProgress(progress);
            System.out.println("✅ Saved progress for " + progress.getUsername() + ": " + saved.getOverallProgress() + "%");
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("❌ Error saving progress: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}