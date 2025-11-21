package com.example.learningpathgenerator.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillProfile {
    private Map<String, String> skillProficiency = new HashMap<>(); // skill -> BEGINNER/INTERMEDIATE/ADVANCED
    private List<String> strengths;
    private List<String> weaknesses;
    private Double learningVelocity;
    private String recommendedLearningStyle; // VISUAL, READING, HANDS_ON, MIXED
    private String currentLevel;
    private Integer availableTimePerWeek;
}