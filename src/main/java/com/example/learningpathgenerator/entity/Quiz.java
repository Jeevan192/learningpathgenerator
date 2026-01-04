package com.example.learningpathgenerator.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String topic;
    private Long userId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // FIX: Added this field to resolve "Cannot resolve method 'getPassingScore'"
    // Defaulting to 6 (assuming 10 questions) to avoid null pointer exceptions in math
    private Integer passingScore = 6;

    @ManyToMany
    private List<Question> questions;
}
