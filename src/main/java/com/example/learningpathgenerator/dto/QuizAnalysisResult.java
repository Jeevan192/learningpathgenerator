package com.example.learningpathgenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizAnalysisResult {
    private double scorePercentage;
    private int correctCount;
    private int totalQuestions;
    private String performance;
    private String feedback;
    private Long learningPathId;
    private SkillProfile skillProfile;
}
