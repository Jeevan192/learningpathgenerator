package com.example.learningpathgenerator.dto;

import lombok.Data;
import java.util.Map;

@Data
public class QuizSubmission {
    private String topic;

    // Key: Question ID, Value: Selected Option (e.g., "A")
    private Map<Long, String> answers;

    // Key: Question ID, Value: Confidence Level (0-100)
    private Map<Long, Integer> confidence;
}
