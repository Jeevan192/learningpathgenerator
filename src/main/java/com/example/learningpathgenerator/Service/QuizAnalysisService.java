package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.dto.QuizAnalysisResult;
import com.example.learningpathgenerator.dto.SkillProfile;
import com.example.learningpathgenerator.entity.QuizAttempt;
import com.example.learningpathgenerator.entity.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAnalysisService {

    public QuizAnalysisResult analyzeQuizAttempt(QuizAttempt attempt) {
        QuizAnalysisResult result = new QuizAnalysisResult();

        // Create skill profile
        SkillProfile skillProfile = createSkillProfile(attempt);
        result.setSkillProfile(skillProfile);

        // Calculate overall score
        double overallScore = (double) attempt.getCorrectAnswers() / attempt.getTotalQuestions() * 100;
        result.setOverallScore(overallScore);

        // Determine performance level
        String performanceLevel;
        if (overallScore >= 90) {
            performanceLevel = "EXCELLENT";
        } else if (overallScore >= 75) {
            performanceLevel = "GOOD";
        } else if (overallScore >= 60) {
            performanceLevel = "AVERAGE";
        } else {
            performanceLevel = "POOR";
        }
        result.setPerformanceLevel(performanceLevel);

        // Generate recommendation
        String recommendation = generateRecommendation(skillProfile, performanceLevel);
        result.setRecommendation(recommendation);

        return result;
    }

    public List<Map<String, Object>> getQuestionAnalysis(QuizAttempt attempt) {
        List<Map<String, Object>> analysis = new ArrayList<>();

        for (Question question : attempt.getQuiz().getQuestions()) {
            Map<String, Object> questionData = new HashMap<>();
            questionData.put("questionId", question.getId());
            questionData.put("questionText", question.getQuestionText());
            
            String userAnswer = attempt.getAnswers().get(String.valueOf(question.getId()));
            questionData.put("userAnswer", userAnswer);
            questionData.put("correctAnswer", question.getCorrectAnswer());
            
            boolean isCorrect = question.getCorrectAnswer().equals(userAnswer);
            questionData.put("correct", isCorrect);
            questionData.put("skillsTested", question.getSkillsTested());

            analysis.add(questionData);
        }

        return analysis;
    }

    public Map<String, Object> getPerformanceComparison(QuizAttempt attempt) {
        Map<String, Object> comparison = new HashMap<>();
        
        double userScore = (double) attempt.getCorrectAnswers() / attempt.getTotalQuestions() * 100;
        comparison.put("userScore", userScore);
        comparison.put("passingScore", (double) attempt.getQuiz().getPassingScore() / attempt.getTotalQuestions() * 100);
        
        // Mock average score (in real implementation, calculate from all attempts)
        comparison.put("averageScore", 70.0);
        
        return comparison;
    }

    private SkillProfile createSkillProfile(QuizAttempt attempt) {
        SkillProfile profile = new SkillProfile();

        // Analyze skill scores from attempt
        Map<String, Double> skillScores = attempt.getSkillScores();
        Map<String, String> skillProficiency = new HashMap<>();

        for (Map.Entry<String, Double> entry : skillScores.entrySet()) {
            String skill = entry.getKey();
            Double score = entry.getValue();

            String level;
            if (score >= 0.8) {
                level = "ADVANCED";
            } else if (score >= 0.6) {
                level = "INTERMEDIATE";
            } else {
                level = "BEGINNER";
            }

            skillProficiency.put(skill, level);
        }

        profile.setSkillProficiency(skillProficiency);

        // Identify strengths and weaknesses
        List<String> strengths = skillScores.entrySet().stream()
                .filter(e -> e.getValue() >= 0.7)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> weaknesses = skillScores.entrySet().stream()
                .filter(e -> e.getValue() < 0.6)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        profile.setStrengths(strengths);
        profile.setWeaknesses(weaknesses);

        // Determine current level
        double avgScore = skillScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.5);

        if (avgScore >= 0.8) {
            profile.setCurrentLevel("ADVANCED");
        } else if (avgScore >= 0.6) {
            profile.setCurrentLevel("INTERMEDIATE");
        } else {
            profile.setCurrentLevel("BEGINNER");
        }

        // Recommend learning style
        profile.setRecommendedLearningStyle("HANDS_ON");

        return profile;
    }

    private String generateRecommendation(SkillProfile profile, String performanceLevel) {
        StringBuilder recommendation = new StringBuilder();

        if ("EXCELLENT".equals(performanceLevel)) {
            recommendation.append("Outstanding performance! ");
        } else if ("GOOD".equals(performanceLevel)) {
            recommendation.append("Good job! ");
        } else if ("AVERAGE".equals(performanceLevel)) {
            recommendation.append("You're making progress! ");
        } else {
            recommendation.append("Don't worry, everyone starts somewhere! ");
        }

        if (profile.getWeaknesses() != null && !profile.getWeaknesses().isEmpty()) {
            recommendation.append("Focus on improving: ")
                    .append(String.join(", ", profile.getWeaknesses()))
                    .append(". ");
        }

        if (profile.getStrengths() != null && !profile.getStrengths().isEmpty()) {
            recommendation.append("Your strengths include: ")
                    .append(String.join(", ", profile.getStrengths()))
                    .append(". ");
        }

        recommendation.append("A personalized learning path has been generated to help you improve.");

        return recommendation.toString();
    }
}
