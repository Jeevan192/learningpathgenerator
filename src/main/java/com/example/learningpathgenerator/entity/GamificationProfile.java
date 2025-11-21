package com.example.learningpathgenerator.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gamification_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamificationProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_points")
    private Integer totalPoints = 0;

    private Integer level = 1;

    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @Column(name = "longest_streak")
    private Integer longestStreak = 0;

    @ElementCollection
    @CollectionTable(name = "user_badges", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "badge")
    private List<String> badges = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_achievements", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "achievement", length = 500)
    private List<String> achievements = new ArrayList<>();

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "quizzes_completed")
    private Integer quizzesCompleted = 0;

    @Column(name = "perfect_scores")
    private Integer perfectScores = 0;

    @Column(name = "resources_completed")
    private Integer resourcesCompleted = 0;

    @Column(name = "paths_completed")
    private Integer pathsCompleted = 0;
}