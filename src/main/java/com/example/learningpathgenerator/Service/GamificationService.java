package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.entity.*;
import com.example.learningpathgenerator.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GamificationService {

    private final UserProgressRepository userProgressRepository;
    private final GamificationProfileRepository gamificationProfileRepository;
    private final UserRepository userRepository;

    // Points configuration
    private static final int QUIZ_BASE_POINTS = 10;
    private static final int RESOURCE_COMPLETION_POINTS = 5;
    private static final int PATH_COMPLETION_POINTS = 50;
    private static final int STREAK_BONUS_POINTS = 2;
    private static final int PERFECT_SCORE_BONUS = 20;

    public Map<String, Object> awardQuizPoints(User user, QuizAttempt attempt) {
        GamificationProfile profile = getOrCreateProfile(user);

        // Calculate points based on quiz performance
        int basePoints = QUIZ_BASE_POINTS;
        double scorePercentage = (double) attempt.getScore() / attempt.getQuiz().getQuestions().size() * 100;

        int earnedPoints = basePoints;

        // Bonus for high scores
        if (scorePercentage >= 100.0) {
            earnedPoints += PERFECT_SCORE_BONUS;
        } else if (scorePercentage >= 80.0) {
            earnedPoints += 10;
        } else if (scorePercentage >= 60.0) {
            earnedPoints += 5;
        }

        // Update profile
        profile.setTotalPoints(profile.getTotalPoints() + earnedPoints);
        profile.setQuizzesCompleted(profile.getQuizzesCompleted() + 1);
        updateLevel(profile);
        updateStreak(profile);

        gamificationProfileRepository.save(profile);

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("pointsEarned", earnedPoints);
        response.put("totalPoints", profile.getTotalPoints());
        response.put("level", profile.getCurrentLevel());
        response.put("streak", profile.getCurrentStreak());
        response.put("badges", profile.getBadges());

        // Check for new achievements
        List<String> newBadges = checkForNewBadges(profile);
        if (!newBadges.isEmpty()) {
            response.put("newBadges", newBadges);
        }

        return response;
    }

    public Map<String, Object> awardResourcePoints(User user, LearningResource resource) {
        GamificationProfile profile = getOrCreateProfile(user);

        int earnedPoints = RESOURCE_COMPLETION_POINTS;

        // Bonus for difficult resources
        if ("ADVANCED".equalsIgnoreCase(resource.getDifficultyLevel())) {
            earnedPoints += 5;
        } else if ("INTERMEDIATE".equalsIgnoreCase(resource.getDifficultyLevel())) {
            earnedPoints += 2;
        }

        profile.setTotalPoints(profile.getTotalPoints() + earnedPoints);
        profile.setResourcesCompleted(profile.getResourcesCompleted() + 1);
        updateLevel(profile);
        updateStreak(profile);

        gamificationProfileRepository.save(profile);

        Map<String, Object> response = new HashMap<>();
        response.put("pointsEarned", earnedPoints);
        response.put("totalPoints", profile.getTotalPoints());
        response.put("level", profile.getCurrentLevel());
        response.put("resourcesCompleted", profile.getResourcesCompleted());

        return response;
    }

    public Map<String, Object> awardPathCompletionPoints(User user, LearningPath path) {
        GamificationProfile profile = getOrCreateProfile(user);

        int earnedPoints = PATH_COMPLETION_POINTS;

        profile.setTotalPoints(profile.getTotalPoints() + earnedPoints);
        profile.setPathsCompleted(profile.getPathsCompleted() + 1);
        updateLevel(profile);

        gamificationProfileRepository.save(profile);

        Map<String, Object> response = new HashMap<>();
        response.put("pointsEarned", earnedPoints);
        response.put("totalPoints", profile.getTotalPoints());
        response.put("level", profile.getCurrentLevel());
        response.put("pathsCompleted", profile.getPathsCompleted());
        response.put("message", "Congratulations on completing the learning path!");

        return response;
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
                    entry.put("badges", profile.getBadges().size());
                    leaderboard.add(entry);
                });

        return leaderboard;
    }

    public Map<String, Object> getUserStats(User user) {
        GamificationProfile profile = getOrCreateProfile(user);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPoints", profile.getTotalPoints());
        stats.put("level", profile.getCurrentLevel());
        stats.put("currentStreak", profile.getCurrentStreak());
        stats.put("longestStreak", profile.getLongestStreak());
        stats.put("quizzesCompleted", profile.getQuizzesCompleted());
        stats.put("resourcesCompleted", profile.getResourcesCompleted());
        stats.put("pathsCompleted", profile.getPathsCompleted());
        stats.put("badges", profile.getBadges());
        stats.put("nextLevelPoints", calculatePointsToNextLevel(profile));

        return stats;
    }

    private GamificationProfile getOrCreateProfile(User user) {
        return gamificationProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    GamificationProfile newProfile = new GamificationProfile();
                    newProfile.setUser(user);
                    newProfile.setTotalPoints(0);
                    newProfile.setCurrentLevel(1);
                    newProfile.setCurrentStreak(0);
                    newProfile.setLongestStreak(0);
                    newProfile.setQuizzesCompleted(0);
                    newProfile.setResourcesCompleted(0);
                    newProfile.setPathsCompleted(0);
                    newProfile.setBadges(new ArrayList<>());
                    newProfile.setLastActivityDate(LocalDateTime.now());
                    return gamificationProfileRepository.save(newProfile);
                });
    }

    private void updateLevel(GamificationProfile profile) {
        // Level calculation: Level = floor(sqrt(totalPoints / 100)) + 1
        int newLevel = (int) Math.floor(Math.sqrt(profile.getTotalPoints() / 100.0)) + 1;
        if (newLevel > profile.getCurrentLevel()) {
            profile.setCurrentLevel(newLevel);
        }
    }

    private void updateStreak(GamificationProfile profile) {
        LocalDateTime lastActivity = profile.getLastActivityDate();
        LocalDateTime now = LocalDateTime.now();

        if (lastActivity != null) {
            long daysBetween = java.time.Duration.between(lastActivity, now).toDays();

            if (daysBetween == 1) {
                // Consecutive day
                profile.setCurrentStreak(profile.getCurrentStreak() + 1);
                if (profile.getCurrentStreak() > profile.getLongestStreak()) {
                    profile.setLongestStreak(profile.getCurrentStreak());
                }
                profile.setTotalPoints(profile.getTotalPoints() + STREAK_BONUS_POINTS);
            } else if (daysBetween > 1) {
                // Streak broken
                profile.setCurrentStreak(1);
            }
        } else {
            profile.setCurrentStreak(1);
        }

        profile.setLastActivityDate(now);
    }

    private List<String> checkForNewBadges(GamificationProfile profile) {
        List<String> newBadges = new ArrayList<>();
        List<String> currentBadges = profile.getBadges();

        // First Quiz Badge
        if (profile.getQuizzesCompleted() >= 1 && !currentBadges.contains("First Steps")) {
            currentBadges.add("First Steps");
            newBadges.add("First Steps");
        }

        // Quiz Master Badge
        if (profile.getQuizzesCompleted() >= 10 && !currentBadges.contains("Quiz Master")) {
            currentBadges.add("Quiz Master");
            newBadges.add("Quiz Master");
        }

        // Learning Enthusiast
        if (profile.getResourcesCompleted() >= 20 && !currentBadges.contains("Learning Enthusiast")) {
            currentBadges.add("Learning Enthusiast");
            newBadges.add("Learning Enthusiast");
        }

        // Path Completer
        if (profile.getPathsCompleted() >= 1 && !currentBadges.contains("Path Completer")) {
            currentBadges.add("Path Completer");
            newBadges.add("Path Completer");
        }

        // Streak Master
        if (profile.getCurrentStreak() >= 7 && !currentBadges.contains("Week Warrior")) {
            currentBadges.add("Week Warrior");
            newBadges.add("Week Warrior");
        }

        // Level Milestones
        if (profile.getCurrentLevel() >= 5 && !currentBadges.contains("Rising Star")) {
            currentBadges.add("Rising Star");
            newBadges.add("Rising Star");
        }

        if (profile.getCurrentLevel() >= 10 && !currentBadges.contains("Expert Learner")) {
            currentBadges.add("Expert Learner");
            newBadges.add("Expert Learner");
        }

        profile.setBadges(currentBadges);

        return newBadges;
    }

    private int calculatePointsToNextLevel(GamificationProfile profile) {
        int currentLevel = profile.getCurrentLevel();
        int pointsForNextLevel = (int) Math.pow(currentLevel * 10, 2);
        return pointsForNextLevel - profile.getTotalPoints();
    }
}