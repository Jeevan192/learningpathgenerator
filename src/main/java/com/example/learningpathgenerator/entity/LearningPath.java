package com.example.learningpathgenerator.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_paths")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningPath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_skill")
    private String targetSkill;

    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    private List<LearningResource> resources = new ArrayList<>();

    @Column(name = "difficulty_level")
    private String difficultyLevel;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // in hours

    @Column(name = "completion_percentage")
    private Double completionPercentage = 0.0;

    private String status; // ACTIVE, COMPLETED, ABANDONED

    @Column(name = "generation_reason", columnDefinition = "TEXT")
    private String generationReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
}