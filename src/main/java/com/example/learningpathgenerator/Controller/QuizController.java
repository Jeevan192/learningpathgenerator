package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.entity.*;
import com.example.learningpathgenerator.dto.*;
import com.example.learningpathgenerator.repository.*;
import com.example.learningpathgenerator.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final QuizAnalysisService quizAnalysisService;
    private final GamificationService gamificationService;
    private final LearningPathService learningPathService;

    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuiz(@PathVariable Long id) {
        return quizRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/topic/{topic}")
    public ResponseEntity<List<Quiz>> getQuizzesByTopic(@PathVariable String topic) {
        return ResponseEntity.ok(quizRepository.findByTopic(topic));
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitQuiz(@RequestBody QuizSubmission submission) {
        // Get user and quiz
        User user = userRepository.findById(submission.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Quiz quiz = quizRepository.findById(submission.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Process submission
        QuizAttempt attempt = processSubmission(user, quiz, submission);

        // Analyze results
        QuizAnalysisResult analysis = quizAnalysisService.analyzeQuizAttempt(attempt);

        // Award gamification points
        Map<String, Object> gamificationRewards = gamificationService.awardQuizPoints(user, attempt);

        // Generate learning path
        LearningPath path = learningPathService.generateLearningPath(user, attempt);

        // Get question-level analysis
        List<Map<String, Object>> questionAnalysis = quizAnalysisService.getQuestionAnalysis(attempt);

        // Get performance comparison
        Map<String, Object> comparison = quizAnalysisService.getPerformanceComparison(attempt);

        Map<String, Object> response = new HashMap<>();
        response.put("attemptId", attempt.getId());
        response.put("analysis", analysis);
        response.put("gamification", gamificationRewards);
        response.put("learningPathId", path.getId());
        response.put("learningPath", path);
        response.put("questionAnalysis", questionAnalysis);
        response.put("comparison", comparison);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<QuizAttempt> getAttempt(@PathVariable Long attemptId) {
        return quizAttemptRepository.findById(attemptId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/attempts")
    public ResponseEntity<List<QuizAttempt>> getUserAttempts(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(quizAttemptRepository.findByUserOrderByCompletedAtDesc(user));
    }

    private QuizAttempt processSubmission(User user, Quiz quiz, QuizSubmission submission) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setAnswers(submission.getAnswers());
        attempt.setStartedAt(LocalDateTime.now().minusSeconds(submission.getTimeTaken()));
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setTimeTaken(submission.getTimeTaken());

        // Calculate score
        int correctAnswers = 0;
        int totalScore = 0;
        Map<String, Double> skillScores = new HashMap<>();
        Map<String, Integer> skillAttempts = new HashMap<>();

        for (Question question : quiz.getQuestions()) {
            String userAnswer = submission.getAnswers().get(question.getId());
            boolean isCorrect = question.getCorrectAnswer().equals(userAnswer);

            if (isCorrect) {
                correctAnswers++;
                totalScore += question.getPoints() != null ? question.getPoints() : 1;

                // Update skill scores
                for (String skill : question.getSkillsTested()) {
                    skillScores.put(skill, skillScores.getOrDefault(skill, 0.0) + 1.0);
                    skillAttempts.put(skill, skillAttempts.getOrDefault(skill, 0) + 1);
                }
            } else {
                // Track attempted skills even if wrong
                for (String skill : question.getSkillsTested()) {
                    skillAttempts.put(skill, skillAttempts.getOrDefault(skill, 0) + 1);
                }
            }
        }

        // Calculate skill proficiency (0-1 scale)
        Map<String, Double> normalizedSkillScores = new HashMap<>();
        for (String skill : skillAttempts.keySet()) {
            double score = skillScores.getOrDefault(skill, 0.0) / skillAttempts.get(skill);
            normalizedSkillScores.put(skill, score);
        }

        attempt.setScore(totalScore);
        attempt.setTotalQuestions(quiz.getQuestions().size());
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setSkillScores(normalizedSkillScores);
        attempt.setPassed(correctAnswers >= quiz.getPassingScore());

        return quizAttemptRepository.save(attempt);
    }
}