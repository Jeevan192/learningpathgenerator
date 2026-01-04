package com.example.learningpathgenerator.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String resourceType; // VIDEO, ARTICLE, QUIZ
    private String url;
    private String provider;
    private Integer sequenceNumber;
    private Integer estimatedDuration; // in minutes

    @ElementCollection
    private List<String> tags;

    private String difficultyLevel;
    private Double relevanceScore;

    @ManyToOne
    @JoinColumn(name = "learning_path_id")
    @JsonBackReference
    private LearningPath learningPath;
}