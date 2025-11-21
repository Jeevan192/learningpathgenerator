package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.entity.*;
import com.example.learningpathgenerator.dto.QuizAnalysisResult;
import com.example.learningpathgenerator.dto.SkillProfile;
import com.example.learningpathgenerator.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAnalysisService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final MLModelService mlModelService;
    private final AIContentService aiContentService;

    @Transactional
    public QuizAnalysisResult analyzeQuizAttempt(QuizAttempt attempt) {
        QuizAnalysisResult result = new QuizAnalysisResult();

        // Calculate basic metrics
        result.setScore(attempt.getScore());
        result.setPercentage((double) attempt.getCorrectAnswers() / attempt.getTotalQuestions() * 100);
        result.setPassed(attempt.getPassed());

        // Analyze skill performance
        Map<String, Double> skillScores = attempt.getSkillScores();
        result.setSkillScores(skillScores);

        // Get historical data for trend analysis
        List<QuizAttempt> historicalAttempts = quizAttemptRepository
                .findByUserOrderByCompletedAtDesc(attempt.getUser());

        List<Map<String, Double>> historicalScores = historicalAttempts.stream()
                .map(QuizAttempt::getSkillScores)
                .collect(Collectors.toList());

        // Use ML to analyze profile
        SkillProfile skillProfile = mlModelService.analyzeQuizResults(skillScores, historicalScores);
        result.setSkillProfile(skillProfile);

        // Identify areas needing improvement
        List<String> areasToImprove = skillScores.entrySet().stream()
                .filter(e -> e.getValue() < 0.6)
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .limit(3)
                .collect(Collectors.toList());
        result.setAreasToImprove(areasToImprove);

        // Generate personalized feedback using AI
        try {
            List<String> tips = aiContentService.generateStudyTips(
                    areasToImprove,
                    skillProfile.getRecommendedLearningStyle(),
                    attempt.getUser().getGamificationProfile() != null ?
                            attempt.getUser().getGamificationProfile().getCurrentStreak() : 0
            );
            String feedback = tips.stream().collect(Collectors.joining("\n"));
            result.setPersonalizedFeedback(feedback);
        } catch (Exception e) {
            log.error("Failed to generate AI feedback", e);
            result.setPersonalizedFeedback("Keep practicing to improve your skills!");
        }

        // Calculate improvement from previous attempts
        if (historicalAttempts.size() > 1) {
            QuizAttempt previous = historicalAttempts.get(1);
            double improvement = attempt.getScore() - previous.getScore();
            result.setImprovementFromLast(improvement);
        }

        // Determine recommended next quiz difficulty
        String nextDifficulty = determineNextDifficulty(attempt, skillProfile);
        result.setRecommendedNextDifficulty(nextDifficulty);

        return result;
    }

    public List<Map<String, Object>> getQuestionAnalysis(QuizAttempt attempt) {
        List<Map<String, Object>> analysis = new ArrayList<>();

        Quiz quiz = attempt.getQuiz();
        Map<Long, String> userAnswers = attempt.getAnswers();

        for (Question question : quiz.getQuestions()) {
            Map<String, Object> questionAnalysis = new HashMap<>();

            questionAnalysis.put("questionId", question.getId());
            questionAnalysis.put("questionText", question.getQuestionText());
            questionAnalysis.put("userAnswer", userAnswers.get(question.getId()));
            questionAnalysis.put("correctAnswer", question.getCorrectAnswer());
            questionAnalysis.put("isCorrect",
                    question.getCorrectAnswer().equals(userAnswers.get(question.getId())));
            questionAnalysis.put("skillsTested", question.getSkillsTested());
            questionAnalysis.put("difficulty", question.getDifficultyLevel());

            // Generate AI explanation for incorrect answers
            if (!question.getCorrectAnswer().equals(userAnswers.get(question.getId()))) {
                try {
                    String explanation = aiContentService.explainAnswer(
                            question.getQuestionText(),
                            question.getCorrectAnswer(),
                            userAnswers.get(question.getId())
                    );
                    questionAnalysis.put("explanation", explanation);
                } catch (Exception e) {
                    log.error("Failed to generate explanation", e);
                    questionAnalysis.put("explanation", "Review this topic for better understanding.");
                }
            }

            analysis.add(questionAnalysis);
        }

        return analysis;
    }

    public Map<String, Object> getPerformanceComparison(QuizAttempt attempt) {
        Map<String, Object> comparison = new HashMap<>();

        // Get all attempts for same quiz
        List<QuizAttempt> allAttempts = quizAttemptRepository
                .findByQuiz(attempt.getQuiz());

        if (allAttempts.isEmpty()) {
            return comparison;
        }

        // Calculate percentile
        long betterThan = allAttempts.stream()
                .filter(a -> a.getScore() < attempt.getScore())
                .count();
        double percentile = (double) betterThan / allAttempts.size() * 100;

        comparison.put("percentile", Math.round(percentile));
        comparison.put("averageScore",
                allAttempts.stream()
                        .mapToInt(QuizAttempt::getScore)
                        .average()
                        .orElse(0.0));
        comparison.put("topScore",
                allAttempts.stream()
                        .mapToInt(QuizAttempt::getScore)
                        .max()
                        .orElse(0));
        comparison.put("totalAttempts", allAttempts.size());

        return comparison;
    }

    private String determineNextDifficulty(QuizAttempt attempt, SkillProfile profile) {
        double percentage = (double) attempt.getCorrectAnswers() / attempt.getTotalQuestions();
        String currentDifficulty = attempt.getQuiz().getDifficultyLevel();

        if (percentage >= 0.9 && !currentDifficulty.equals("ADVANCED")) {
            return "ADVANCED";
        } else if (percentage >= 0.7 && currentDifficulty.equals("BEGINNER")) {
            return "INTERMEDIATE";
        } else if (percentage < 0.5 && !currentDifficulty.equals("BEGINNER")) {
            return "BEGINNER";
        }

        return currentDifficulty;
    }
}