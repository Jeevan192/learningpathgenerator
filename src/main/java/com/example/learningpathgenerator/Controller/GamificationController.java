package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.entity.GamificationProfile;
import com.example.learningpathgenerator.entity.User;
import com.example.learningpathgenerator.repository.GamificationProfileRepository;
import com.example.learningpathgenerator.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/gamification")
@CrossOrigin(origins = "http://localhost:3000")
public class GamificationController {

    private final GamificationProfileRepository gamificationRepository;
    private final UserRepository userRepository;

    public GamificationController(GamificationProfileRepository gamificationRepository, UserRepository userRepository) {
        this.gamificationRepository = gamificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = ((UserDetails) auth.getPrincipal()).getUsername();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User user = userOpt.get();

        GamificationProfile profile = gamificationRepository.findByUser(user)
                .orElseGet(() -> {
                    GamificationProfile newProfile = new GamificationProfile();
                    newProfile.setUser(user);
                    return gamificationRepository.save(newProfile);
                });

        return ResponseEntity.ok(Map.of(
                "points", profile.getTotalPoints() != null ? profile.getTotalPoints() : 0,
                "level", profile.getCurrentLevel() != null ? profile.getCurrentLevel() : 1,
                "badges", 0,
                "streak", profile.getStreak() != null ? profile.getStreak() : 0
        ));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<GamificationProfile>> getLeaderboard() {
        List<GamificationProfile> profiles = gamificationRepository.findAll();
        profiles.sort((a, b) -> {
            Integer pointsA = a.getTotalPoints() != null ? a.getTotalPoints() : 0;
            Integer pointsB = b.getTotalPoints() != null ? b.getTotalPoints() : 0;
            return pointsB.compareTo(pointsA);
        });
        return ResponseEntity.ok(profiles);
    }
}

