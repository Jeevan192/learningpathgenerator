package com.example.learningpathgenerator.dto;

import com.example.learningpathgenerator.model.LearningPath;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathSaveRequest {
    private String username;
    private LearningPath learningPath;
}