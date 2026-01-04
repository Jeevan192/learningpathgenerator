package com.example.learningpathgenerator.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String targetSkill;
    private String status; // e.g., ACTIVE, COMPLETED
    private String difficultyLevel;

    @Column(length = 2000)
    private String generationReason;

    private Double completionPercentage;
    private Integer estimatedDuration; // in minutes or hours based on service logic

    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LearningResource> resources;
}