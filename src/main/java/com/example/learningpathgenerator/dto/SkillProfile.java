package com.example.learningpathgenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillProfile {
    private String currentLevel;
    private List<String> strengths;
    private List<String> weaknesses;
    private String recommendedLearningStyle;
    private Map<String, String> skillProficiency;
}
