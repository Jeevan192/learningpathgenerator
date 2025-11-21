package com.example.learningpathgenerator.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "gamification_profiles")
public class GamificationProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private Integer totalPoints;
    private Integer currentLevel;
    private Integer quizzesCompleted;
    private Integer pathsCompleted;
    private Integer resourcesCompleted;
    private Integer streak;
    private LocalDate lastActivityDate;

    public GamificationProfile() {
        this.totalPoints = 0;
        this.currentLevel = 1;
        this.quizzesCompleted = 0;
        this.pathsCompleted = 0;
        this.resourcesCompleted = 0;
        this.streak = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Integer currentLevel) {
        this.currentLevel = currentLevel;
    }

    public Integer getQuizzesCompleted() {
        return quizzesCompleted;
    }

    public void setQuizzesCompleted(Integer quizzesCompleted) {
        this.quizzesCompleted = quizzesCompleted;
    }

    public Integer getPathsCompleted() {
        return pathsCompleted;
    }

    public void setPathsCompleted(Integer pathsCompleted) {
        this.pathsCompleted = pathsCompleted;
    }

    public Integer getResourcesCompleted() {
        return resourcesCompleted;
    }

    public void setResourcesCompleted(Integer resourcesCompleted) {
        this.resourcesCompleted = resourcesCompleted;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(LocalDate lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }
}
