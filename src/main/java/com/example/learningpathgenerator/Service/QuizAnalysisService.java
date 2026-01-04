package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.dto.QuizAnalysisResult;
import com.example.learningpathgenerator.entity.Question;
import com.example.learningpathgenerator.entity.QuizAttempt;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAnalysisService {

    private final AIContentService aiContentService;
    private final ObjectMapper objectMapper;

    public QuizAnalysisResult analyzeQuizAttempt(QuizAttempt attempt) {
        try {
            // 1. Prepare context for AI (Questions, User Answers, Correct Answers)
            List<Map<String, Object>> quizContext = new ArrayList<>();
            for (Question q : attempt.getQuiz().getQuestions()) {
                Map<String, Object> item = new HashMap<>();
                item.put("question", q.getQuestionText());
                item.put("correctAnswer", q.getCorrectAnswer());
                // Assuming answers are stored by Question ID as String
                item.put("userAnswer", attempt.getAnswers().get(String.valueOf(q.getId())));
                item.put("skills", q.getSkillsTested());
                quizContext.add(item);
            }

            String jsonContext = objectMapper.writeValueAsString(quizContext);

            // 2. Call AI Service to analyze performance
            // The AI is expected to return a JSON matching QuizAnalysisResult structure
            // (specifically populating skillProfile, feedback, and performance)
            String aiResponse = aiContentService.analyzeQuizPerformance(jsonContext);

            QuizAnalysisResult result = objectMapper.readValue(aiResponse, QuizAnalysisResult.class);

            // 3. Override calculated fields to ensure mathematical accuracy
            // (We trust code over AI for simple math)
            int correct = attempt.getCorrectAnswers();
            int total = attempt.getTotalQuestions();
            double score = total > 0 ? (double) correct / total * 100 : 0;

            result.setCorrectCount(correct);
            result.setTotalQuestions(total);
            result.setScorePercentage(score);

            return result;

        } catch (Exception e) {
            log.error("AI Analysis failed, falling back to basic calculation", e);
            return fallbackAnalysis(attempt);
        }
    }

    private QuizAnalysisResult fallbackAnalysis(QuizAttempt attempt) {
        QuizAnalysisResult result = new QuizAnalysisResult();
        double score = (double) attempt.getCorrectAnswers() / attempt.getTotalQuestions() * 100;

        result.setScorePercentage(score);
        result.setCorrectCount(attempt.getCorrectAnswers());
        result.setTotalQuestions(attempt.getTotalQuestions());
        result.setPerformance(score > 75 ? "GOOD" : "AVERAGE");
        result.setFeedback("AI Analysis unavailable at this time. Your score is " + String.format("%.2f", score) + "%.");

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

        // Ensure your Quiz entity has the 'passingScore' field
        comparison.put("passingScore", (double) attempt.getQuiz().getPassingScore() / attempt.getTotalQuestions() * 100);

        // Mock average score (in real implementation, calculate from all attempts)
        comparison.put("averageScore", 70.0);

        return comparison;
    }
}
