package com.example.learningpathgenerator.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", length = 1000)
    private String questionText;

    @Column(name = "topic")
    private String topic;

    @Column(name = "option_a")
    private String optionA;

    @Column(name = "option_b")
    private String optionB;

    @Column(name = "option_c")
    private String optionC;

    @Column(name = "option_d")
    private String optionD;

    @Column(name = "correct_answer")
    private String correctAnswer;

    @Column(name = "explanation", length = 1000)
    private String explanation;

    @Column(name = "difficulty")
    private String difficulty;

    @Column(name = "skills_tested")
    private String skillsTested;
}
