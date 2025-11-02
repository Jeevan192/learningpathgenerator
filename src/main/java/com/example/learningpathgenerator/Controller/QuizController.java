package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.dto.GenerateRequest;
import com.example.learningpathgenerator.model.*;
import com.example.learningpathgenerator.entity.UserProgress;
import com.example.learningpathgenerator.Service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizService quizService;
    private final LearningPathService learningPathService;
    private final UserLearningPathService userLearningPathService;
    private final UserProgressService userProgressService;
    private final ObjectMapper objectMapper;

    public QuizController(QuizService quizService,
                          LearningPathService learningPathService,
                          UserLearningPathService userLearningPathService,
                          UserProgressService userProgressService,
                          ObjectMapper objectMapper) {
        this.quizService = quizService;
        this.learningPathService = learningPathService;
        this.userLearningPathService = userLearningPathService;
        this.userProgressService = userProgressService;
        this.objectMapper = objectMapper;
    }

    // ========== PUBLIC QUIZ ENDPOINTS FOR USERS ==========

    @GetMapping(value = "/api/quiz/topics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Topic>> topics() {
        try {
            List<Topic> topics = quizService.getAllTopics();

            System.out.println("\n========================================");
            System.out.println("📞 API Call: GET /api/quiz/topics");
            System.out.println("✅ Retrieved " + topics.size() + " topics from service");
            System.out.println("========================================");

            topics.forEach(t -> {
                System.out.println("📚 Topic Details:");
                System.out.println("   ID: " + t.getId());
                System.out.println("   Name: " + t.getName());
                System.out.println("   Description: " +
                        (t.getDescription() != null ?
                                t.getDescription().substring(0, Math.min(50, t.getDescription().length())) + "..."
                                : "null"));
                System.out.println("   ---");
            });

            // Debug: Print as JSON
            try {
                String json = objectMapper.writeValueAsString(topics);
                System.out.println("📤 Sending JSON Response:");
                System.out.println(json.substring(0, Math.min(500, json.length())) + "...");
            } catch (Exception e) {
                System.err.println("⚠️ Could not serialize for debug: " + e.getMessage());
            }

            System.out.println("========================================\n");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(topics);

        } catch (Exception e) {
            System.err.println("❌ Error in /api/quiz/topics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/api/quiz/topics/{topicId}/questions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Question>> getQuestionsByTopic(@PathVariable String topicId) {
        try {
            System.out.println("\n========================================");
            System.out.println("📞 API Call: GET /api/quiz/topics/" + topicId + "/questions");
            System.out.println("========================================");

            List<Question> questions = quizService.getQuestionsByTopicId(topicId);

            System.out.println("✅ Retrieved " + questions.size() + " questions for topic: " + topicId);

            questions.forEach(q -> {
                System.out.println("❓ Question: " + q.getId() + " - " +
                        (q.getText().length() > 50 ? q.getText().substring(0, 50) + "..." : q.getText()));
            });

            System.out.println("========================================\n");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(questions);

        } catch (Exception e) {
            System.err.println("❌ Error getting questions for topic " + topicId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/quiz/{topicId}")
    public ResponseEntity<?> getQuiz(@PathVariable String topicId) {
        System.out.println("📞 API Call: GET /api/quiz/" + topicId);
        return quizService.findByTopicId(topicId)
                .map(quiz -> {
                    System.out.println("✅ Found quiz for topic: " + topicId);
                    return ResponseEntity.ok(quiz);
                })
                .orElseGet(() -> {
                    System.err.println("❌ Quiz not found for topic: " + topicId);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping("/api/quiz/{topicId}/submit")
    public ResponseEntity<?> submitQuiz(@PathVariable String topicId, @RequestBody Map<String, Object> payload) {
        System.out.println("📞 API Call: POST /api/quiz/" + topicId + "/submit");

        var quizOpt = quizService.findByTopicId(topicId);
        if (quizOpt.isEmpty()) {
            System.err.println("❌ Quiz not found for topic: " + topicId);
            return ResponseEntity.notFound().build();
        }

        Quiz quiz = quizOpt.get();

        @SuppressWarnings("unchecked")
        Map<String, Integer> answers = (Map<String, Integer>) payload.get("answers");
        String name = (String) payload.getOrDefault("name", null);
        String target = (String) payload.getOrDefault("target", null);
        int weeklyHours = payload.get("weeklyHours") == null ? 5 : ((Number) payload.get("weeklyHours")).intValue();

        if (answers == null) {
            System.err.println("❌ No answers provided in submission");
            return ResponseEntity.badRequest().body(Map.of("error", "answers missing"));
        }

        int total = quiz.getQuestions().size();
        int correct = 0;
        for (Question q : quiz.getQuestions()) {
            Integer chosen = answers.get(q.getId());
            if (chosen != null && chosen == q.getCorrectIndex()) correct++;
        }
        double score = (double) correct / total;

        System.out.println("📊 Quiz Results: " + correct + "/" + total + " (" + (int)(score * 100) + "%)");

        String inferredSkill = score >= 0.8 ? "ADVANCED" : score >= 0.5 ? "INTERMEDIATE" : "BEGINNER";
        List<String> interests = List.of(quiz.getTopicName().toLowerCase());

        GenerateRequest genReq = new GenerateRequest();
        genReq.setName(name);
        genReq.setSkillLevel(inferredSkill);
        genReq.setInterests(interests);
        genReq.setWeeklyHours(weeklyHours);
        genReq.setTarget(target);

        LearningPath path = learningPathService.generatePath(genReq);
        UserProgress savedProgress = null;

        if (path != null && name != null && !name.trim().isEmpty()) {
            try {
                userLearningPathService.saveLearningPath(name, path);
                UserProgress progress = new UserProgress();
                progress.setUsername(name);
                progress.setCompletedModules(new ArrayList<>());
                progress.setCurrentModule(0);
                progress.setOverallProgress(0.0);
                progress.setTotalModules(path.getmodules() != null ? path.getmodules().size() : 0);
                savedProgress = userProgressService.saveProgress(progress);
                System.out.println("💾 Saved learning path and progress for user: " + name);
            } catch (Exception e) {
                System.err.println("❌ Error saving learning path: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("score", score);
        response.put("correct", correct);
        response.put("total", total);
        response.put("inferredSkill", inferredSkill);
        response.put("learningPath", path);
        response.put("progress", savedProgress);

        System.out.println("✅ Quiz submission complete");
        return ResponseEntity.ok(response);
    }

    // ========== ADMIN ENDPOINTS ==========

    @GetMapping("/api/admin/quiz/topics")
    public ResponseEntity<?> getAllTopics(@RequestHeader(value = "Role", required = false) String role) {
        System.out.println("📞 ADMIN API Call: GET /api/admin/quiz/topics");

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            System.err.println("❌ Access denied - Role: " + role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        try {
            List<Topic> topics = quizService.getAllTopics();
            System.out.println("✅ ADMIN - Retrieved " + topics.size() + " topics");
            return ResponseEntity.ok(topics);
        } catch (Exception e) {
            System.err.println("❌ ADMIN Error getting topics: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/quiz/topics")
    public ResponseEntity<?> addTopic(@RequestBody Topic topic, @RequestHeader(value = "Role", required = false) String role) {
        System.out.println("📞 ADMIN API Call: POST /api/admin/quiz/topics");

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            System.err.println("❌ Access denied - Role: " + role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        try {
            quizService.addTopic(topic);
            System.out.println("✅ ADMIN - Added topic: " + topic.getName());
            return ResponseEntity.ok(Map.of("message", "Topic added successfully"));
        } catch (Exception e) {
            System.err.println("❌ ADMIN Error adding topic: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/quiz/topics/{topicId}")
    public ResponseEntity<?> updateTopic(@PathVariable String topicId, @RequestBody Topic topic, @RequestHeader(value = "Role", required = false) String role) {
        System.out.println("📞 ADMIN API Call: PUT /api/admin/quiz/topics/" + topicId);

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            System.err.println("❌ Access denied - Role: " + role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        try {
            quizService.updateTopic(topicId, topic);
            System.out.println("✅ ADMIN - Updated topic: " + topicId);
            return ResponseEntity.ok(Map.of("message", "Topic updated successfully"));
        } catch (Exception e) {
            System.err.println("❌ ADMIN Error updating topic: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/quiz/topics/{topicId}")
    public ResponseEntity<?> deleteTopic(@PathVariable String topicId, @RequestHeader(value = "Role", required = false) String role) {
        System.out.println("📞 ADMIN API Call: DELETE /api/admin/quiz/topics/" + topicId);

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            System.err.println("❌ Access denied - Role: " + role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        try {
            quizService.deleteTopic(topicId);
            System.out.println("🗑️ ADMIN - Deleted topic: " + topicId);
            return ResponseEntity.ok(Map.of("message", "Topic deleted successfully"));
        } catch (Exception e) {
            System.err.println("❌ ADMIN Error deleting topic: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/admin/quiz/topics/{topicId}/questions")
    public ResponseEntity<?> getQuestions(@PathVariable String topicId, @RequestHeader(value = "Role", required = false) String role) {
        System.out.println("📞 ADMIN API Call: GET /api/admin/quiz/topics/" + topicId + "/questions");

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            System.err.println("❌ Access denied - Role: " + role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        try {
            List<Question> questions = quizService.getQuestionsByTopicId(topicId);
            System.out.println("✅ ADMIN - Retrieved " + questions.size() + " questions for topic: " + topicId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            System.err.println("❌ ADMIN Error getting questions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/quiz/topics/{topicId}/questions")
    public ResponseEntity<?> addQuestion(@PathVariable String topicId, @RequestBody Question question, @RequestHeader(value = "Role", required = false) String role) {
        System.out.println("📞 ADMIN API Call: POST /api/admin/quiz/topics/" + topicId + "/questions");

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            System.err.println("❌ Access denied - Role: " + role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        try {
            quizService.addQuestionToTopic(topicId, question);
            System.out.println("✅ ADMIN - Added question to topic: " + topicId);
            return ResponseEntity.ok(Map.of("message", "Question added successfully"));
        } catch (Exception e) {
            System.err.println("❌ ADMIN Error adding question: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/quiz/questions/{questionId}")
    public ResponseEntity<?> updateQuestion(@PathVariable String questionId, @RequestBody Question question, @RequestHeader(value = "Role", required = false) String role) {
        System.out.println("📞 ADMIN API Call: PUT /api/admin/quiz/questions/" + questionId);

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            System.err.println("❌ Access denied - Role: " + role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        try {
            quizService.updateQuestion(questionId, question);
            System.out.println("✅ ADMIN - Updated question: " + questionId);
            return ResponseEntity.ok(Map.of("message", "Question updated successfully"));
        } catch (Exception e) {
            System.err.println("❌ ADMIN Error updating question: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/quiz/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable String questionId, @RequestHeader(value = "Role", required = false) String role) {
        System.out.println("📞 ADMIN API Call: DELETE /api/admin/quiz/questions/" + questionId);

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            System.err.println("❌ Access denied - Role: " + role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        try {
            quizService.deleteQuestion(questionId);
            System.out.println("🗑️ ADMIN - Deleted question: " + questionId);
            return ResponseEntity.ok(Map.of("message", "Question deleted successfully"));
        } catch (Exception e) {
            System.err.println("❌ ADMIN Error deleting question: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}