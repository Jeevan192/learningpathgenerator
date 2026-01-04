package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.entity.*;
import com.example.learningpathgenerator.repository.GamificationProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {

    private final GamificationProfileRepository gamificationProfileRepository;

    @Transactional
    public Map<String, Object> awardQuizPoints(User user, QuizAttempt attempt) {
        GamificationProfile profile = getOrCreateProfile(user);

        int basePoints = attempt.getScore() * 10;
        int bonusPoints = 0;

        // Bonus for perfect score
        if (attempt.getCorrectAnswers().equals(attempt.getTotalQuestions())) {
            bonusPoints += 50;
        }

        // Bonus for passing
        if (attempt.getPassed()) {
            bonusPoints += 25;
        }

        int totalPoints = basePoints + bonusPoints;
        profile.setTotalPoints(profile.getTotalPoints() + totalPoints);
        profile.setQuizzesCompleted(profile.getQuizzesCompleted() + 1);

        updateStreak(profile);
        updateLevel(profile);

        gamificationProfileRepository.save(profile);

        Map<String, Object> rewards = new HashMap<>();
        rewards.put("pointsEarned", totalPoints);
        rewards.put("basePoints", basePoints);
        rewards.put("bonusPoints", bonusPoints);
        rewards.put("totalPoints", profile.getTotalPoints());
        rewards.put("currentLevel", profile.getCurrentLevel());
        rewards.put("streak", profile.getStreak());

        return rewards;
    }

    @Transactional
    public Map<String, Object> awardResourcePoints(User user, LearningResource resource) {
        GamificationProfile profile = getOrCreateProfile(user);

        int points = 20; // Base points for completing a resource
        profile.setTotalPoints(profile.getTotalPoints() + points);
        profile.setResourcesCompleted(profile.getResourcesCompleted() + 1);

        updateStreak(profile);
        updateLevel(profile);

        gamificationProfileRepository.save(profile);

        Map<String, Object> rewards = new HashMap<>();
        rewards.put("pointsEarned", points);
        rewards.put("totalPoints", profile.getTotalPoints());
        rewards.put("currentLevel", profile.getCurrentLevel());
        rewards.put("streak", profile.getStreak());

        return rewards;
    }

    @Transactional
    public Map<String, Object> awardPathCompletionPoints(User user, LearningPath path) {
        GamificationProfile profile = getOrCreateProfile(user);

        int points = 100; // Base points for completing a path
        profile.setTotalPoints(profile.getTotalPoints() + points);
        profile.setPathsCompleted(profile.getPathsCompleted() + 1);

        updateStreak(profile);
        updateLevel(profile);

        gamificationProfileRepository.save(profile);

        Map<String, Object> rewards = new HashMap<>();
        rewards.put("pointsEarned", points);
        rewards.put("totalPoints", profile.getTotalPoints());
        rewards.put("currentLevel", profile.getCurrentLevel());
        rewards.put("streak", profile.getStreak());

        return rewards;
    }

    public GamificationProfile getUserProfile(User user) {
        return getOrCreateProfile(user);
    }

    public List<Map<String, Object>> getLeaderboard(int limit) {
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        gamificationProfileRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()))
                .limit(limit)
                .forEach(profile -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("username", profile.getUser().getUsername());
                    entry.put("points", profile.getTotalPoints());
                    entry.put("level", profile.getCurrentLevel());
                    entry.put("streak", profile.getStreak());
                    leaderboard.add(entry);
                });

        return leaderboard;
    }

    public Map<String, Object> getUserStats(User user) {
        GamificationProfile profile = getOrCreateProfile(user);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPoints", profile.getTotalPoints());
        stats.put("currentLevel", profile.getCurrentLevel());
        stats.put("streak", profile.getStreak());
        stats.put("quizzesCompleted", profile.getQuizzesCompleted());
        stats.put("resourcesCompleted", profile.getResourcesCompleted());
        stats.put("pathsCompleted", profile.getPathsCompleted());
        stats.put("rank", getUserRank(user));

        return stats;
    }

    public int getUserRank(User user) {
        GamificationProfile profile = getOrCreateProfile(user);
        long rank = gamificationProfileRepository.findAll().stream()
                .filter(p -> p.getTotalPoints() > profile.getTotalPoints())
                .count();
        return (int) rank + 1;
    }

    private GamificationProfile getOrCreateProfile(User user) {
        return gamificationProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    GamificationProfile profile = new GamificationProfile();
                    profile.setUser(user);
                    profile.setTotalPoints(0);
                    profile.setCurrentLevel(1);
                    profile.setStreak(0);
                    profile.setQuizzesCompleted(0);
                    profile.setResourcesCompleted(0);
                    profile.setPathsCompleted(0);
                    return gamificationProfileRepository.save(profile);
                });
    }

    private void updateStreak(GamificationProfile profile) {
        LocalDate today = LocalDate.now();
        LocalDate lastActivity = profile.getLastActivityDate();

        if (lastActivity == null) {
            profile.setStreak(1);
        } else if (lastActivity.equals(today)) {
            // Same day, don't change streak
            return;
        } else if (lastActivity.equals(today.minusDays(1))) {
            // Consecutive day
            profile.setStreak(profile.getStreak() + 1);
        } else {
            // Streak broken
            profile.setStreak(1);
        }

        profile.setLastActivityDate(today);
    }

    private void updateLevel(GamificationProfile profile) {
        int points = profile.getTotalPoints();
        int newLevel = (points / 100) + 1; // Level up every 100 points
        profile.setCurrentLevel(newLevel);
    }
}