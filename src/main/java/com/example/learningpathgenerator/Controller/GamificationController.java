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
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GamificationController {

    private final GamificationService gamificationService;
    private final UserRepository userRepository;

    @GetMapping("/user/{userId}/profile")
    public ResponseEntity<GamificationProfile> getUserProfile(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(gamificationService.getUserProfile(user));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(gamificationService.getLeaderboard(limit));
    }

    @GetMapping("/user/{userId}/rank")
    public ResponseEntity<Map<String, Object>> getUserRank(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        int rank = gamificationService.getUserRank(user);
        GamificationProfile profile = gamificationService.getUserProfile(user);

        return ResponseEntity.ok(Map.of(
                "rank", rank,
                "totalPoints", profile.getTotalPoints(),
                "level", profile.getLevel()
        ));
    }
}