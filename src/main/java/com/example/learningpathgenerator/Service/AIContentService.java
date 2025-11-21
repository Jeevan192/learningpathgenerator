package com.example.learningpathgenerator.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AIContentService {

    public String generateLearningPathDescription(
            String targetSkill,
            List<String> weaknesses,
            List<String> strengths,
            String learningStyle) {

        log.info("Generating learning path description for skill: {}", targetSkill);

        StringBuilder description = new StringBuilder();
        description.append("This personalized learning path is designed to help you master ")
                .append(targetSkill)
                .append(". ");

        if (weaknesses != null && !weaknesses.isEmpty()) {
            description.append("The path focuses on improving your skills in ")
                    .append(String.join(", ", weaknesses))
                    .append(". ");
        }

        if (strengths != null && !strengths.isEmpty()) {
            description.append("Building on your existing strengths in ")
                    .append(String.join(", ", strengths))
                    .append(", ");
        }

        description.append("this path provides a curated selection of resources ");

        if ("HANDS_ON".equals(learningStyle)) {
            description.append("with emphasis on practical exercises and projects ");
        }

        description.append("to help you achieve your learning goals effectively.");

        return description.toString();
    }
}
