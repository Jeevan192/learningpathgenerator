package com.example.learningpathgenerator.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmission {
    private Long userId;
    private Long quizId;
    private Map<Long, String> answers; // questionId -> answer
    private Integer timeTaken; // in seconds
}