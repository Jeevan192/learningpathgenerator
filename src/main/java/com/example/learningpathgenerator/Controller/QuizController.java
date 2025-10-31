package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.dto.GenerateRequest;
import com.example.learningpathgenerator.model.*;
import com.example.learningpathgenerator.entity.UserProgress;
import com.example.learningpathgenerator.Service.*;
import com.example.learningpathgenerator.Service.UserLearningPathService;
import com.example.learningpathgenerator.Service.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*")
public class QuizController {

    private static final Logger logger = Logger.getLogger(QuizController.class.getName());

    private final QuizService quizService;
    private final LearningPathService learningPathService;

    @Autowired
    private UserLearningPathService userLearningPathService;

    @Autowired
    private UserProgressService userProgressService;

    public QuizController(QuizService quizService, LearningPathService learningPathService) {
        this.quizService = quizService;
        this.learningPathService = learningPathService;
    }

    @GetMapping("/topics")
    public ResponseEntity<Set<String>> topics() {
        return ResponseEntity.ok(quizService.availableTopicIds());
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<?> getQuiz(@PathVariable String topicId) {
        var q = quizService.findByTopicId(topicId);
        return q.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Submit answers: map of questionId -> chosenIndex (0-based)
    // Also accept weeklyHours and name via request params/body to produce the learning path directly
    @PostMapping("/{topicId}/submit")
    public ResponseEntity<?> submitQuiz(@PathVariable String topicId,
                                        @RequestBody Map<String, Object> payload) {
        var quizOpt = quizService.findByTopicId(topicId);
        if (quizOpt.isEmpty()) return ResponseEntity.notFound().build();
        Quiz quiz = quizOpt.get();

        // payload expected:
        // { "answers": { "q1": 1, "q2": 0, ... }, "weeklyHours": 6, "name": "Jeevan192", "target": "backend developer" }
        @SuppressWarnings("unchecked")
        Map<String, Integer> answers = (Map<String, Integer>) payload.get("answers");
        int weeklyHours = payload.get("weeklyHours") == null ? 5 : (int) ((Number) payload.get("weeklyHours")).intValue();
        String name = (String) payload.getOrDefault("name", null);
        String target = (String) payload.getOrDefault("target", null);

        if (answers == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "answers missing"));
        }

        logger.info("Quiz submission received for topic: " + topicId + " by user: " + name);

        int total = quiz.getQuestions().size();
        int correct = 0;
        for (Question q : quiz.getQuestions()) {
            Integer chosen = answers.get(q.getId());
            if (chosen != null && chosen == q.getCorrectIndex()) correct++;
        }
        double score = (double) correct / total;

        // Infer skill level by score thresholds (simple heuristic)
        String inferredSkill;
        if (score >= 0.8) inferredSkill = "ADVANCED";
        else if (score >= 0.5) inferredSkill = "INTERMEDIATE";
        else inferredSkill = "BEGINNER";

        // Use topic tag as interest
        List<String> interests = List.of(quiz.getTopicName().toLowerCase());

        GenerateRequest genReq = new GenerateRequest();
        genReq.setName(name);
        genReq.setSkillLevel(inferredSkill);
        genReq.setInterests(interests);
        genReq.setWeeklyHours(weeklyHours);
        genReq.setTarget(target);

        var path = learningPathService.generatePath(genReq);

        // ========== SAVE TO DATABASE ==========
        if (path != null && name != null && !name.isEmpty()) {
            try {
                logger.info("Saving learning path for user: " + name);

                // Save learning path
                userLearningPathService.saveLearningPath(name, path);

                // Initialize and save progress
                UserProgress progress = new UserProgress();
                progress.setUsername(name);
                progress.setCompletedModules(new ArrayList<>());
                progress.setCurrentModule(0);
                progress.setOverallProgress(0.0);
                progress.setTotalModules(path.getmodules() != null ? path.getmodules().size() : 0);

                userProgressService.saveProgress(progress);

                logger.info("Successfully saved learning path and initialized progress for user: " + name);
            } catch (Exception e) {
                logger.severe("Error saving learning path for user " + name + ": " + e.getMessage());
                e.printStackTrace();
                // Don't fail the request, just log the error
            }
        } else {
            logger.warning("Cannot save learning path - name is null or empty");
        }
        // ========================================

        Map<String, Object> resp = new HashMap<>();
        resp.put("score", score);
        resp.put("correct", correct);
        resp.put("total", total);
        resp.put("inferredSkill", inferredSkill);
        resp.put("learningPath", path);

        return ResponseEntity.ok(resp);
    }
}