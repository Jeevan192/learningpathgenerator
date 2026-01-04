package com.example.learningpathgenerator.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000) // Allow longer descriptions
    private String description;

    private String resourceUrl;
    private String estimatedTime;

    @ManyToOne
    @JoinColumn(name = "learning_path_id")
    @JsonBackReference // Prevents infinite recursion in JSON
    private LearningPath learningPath;
}
