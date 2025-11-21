package com.example.learningpathgenerator.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnalysisResult {
    private Integer score;
    private Double percentage;
    private Boolean passed;
    private Map<String, Double> skillScores;
    private SkillProfile skillProfile;
    private List<String> areasToImprove;
    private String personalizedFeedback;
    private Double improvementFromLast;
    private String recommendedNextDifficulty;
}