package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.entity.*;
import com.example.learningpathgenerator.repository.*;
import com.example.learningpathgenerator.Service.AIContentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final GamificationProfileRepository gamificationRepository;
    private final LearningPathRepository learningPathRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AIContentService aiContentService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    // ============ STATISTICS ============
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalTopics", topicRepository.count());
        stats.put("totalQuestions", questionRepository.count());
        stats.put("totalQuizzes", quizAttemptRepository.count());
        stats.put("totalPaths", learningPathRepository.count());

        // Calculate average quiz score
        List<QuizAttempt> attempts = quizAttemptRepository.findAll();
        if (!attempts.isEmpty()) {
            double avgScore = attempts.stream()
                    .mapToInt(a -> a.getScore() != null ? a.getScore() : 0)
                    .average()
                    .orElse(0.0);
            stats.put("averageQuizScore", Math.round(avgScore * 100.0) / 100.0);
        } else {
            stats.put("averageQuizScore", 0);
        }

        return ResponseEntity.ok(stats);
    }

    // ============ USER MANAGEMENT ============
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        log.info("Fetching all users...");
        try {
            List<User> users = userRepository.findAllByOrderByIdDesc();
            log.info("Found {} users", users.size());

            List<Map<String, Object>> userList = users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
                userMap.put("role", user.getRole() != null ? user.getRole().name() : "USER");
                userMap.put("enabled", user.getEnabled() != null ? user.getEnabled() : true);
                userMap.put("createdAt", user.getCreatedAt());
                userMap.put("lastLoginAt", user.getLastLoginAt());

                // Get gamification stats with error handling
                try {
                    gamificationRepository.findByUser(user).ifPresent(profile -> {
                        userMap.put("quizzesCompleted", profile.getQuizzesCompleted() != null ? profile.getQuizzesCompleted() : 0);
                        userMap.put("totalPoints", profile.getTotalPoints() != null ? profile.getTotalPoints() : 0);
                        userMap.put("level", profile.getCurrentLevel() != null ? profile.getCurrentLevel() : 1);
                    });
                } catch (Exception e) {
                    log.warn("Error getting gamification for user {}: {}", user.getUsername(), e.getMessage());
                    userMap.put("quizzesCompleted", 0);
                    userMap.put("totalPoints", 0);
                    userMap.put("level", 1);
                }

                return userMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Integer userId, @RequestBody Map<String, String> body) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        String newRole = body.get("role");
        if (newRole != null) {
            user.setRole(Role.valueOf(newRole.toUpperCase()));
            userRepository.save(user);
        }

        return ResponseEntity.ok(Map.of("message", "Role updated successfully"));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Integer userId, @RequestBody Map<String, Boolean> body) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        Boolean enabled = body.get("enabled");
        if (enabled != null) {
            user.setEnabled(enabled);
            userRepository.save(user);
        }

        return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }

        // Delete related gamification profile first
        userRepository.findById(userId).ifPresent(user -> {
            gamificationRepository.findByUser(user).ifPresent(gamificationRepository::delete);
        });

        userRepository.deleteById(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // ============ TOPIC MANAGEMENT ============
    @GetMapping("/topics")
    public ResponseEntity<List<TopicEntity>> getAllTopics() {
        return ResponseEntity.ok(topicRepository.findAll());
    }

    @PostMapping("/topics")
    public ResponseEntity<?> createTopic(@RequestBody Map<String, String> body) {
        String name = body.get("name") != null ? body.get("name") : body.get("title");
        String description = body.get("description");

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Topic name is required");
        }

        TopicEntity topic = new TopicEntity(name.trim(), description);
        TopicEntity saved = topicRepository.save(topic);

        log.info("Created new topic: {}", name);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/topics/{topicId}")
    public ResponseEntity<?> updateTopic(@PathVariable Long topicId, @RequestBody Map<String, String> body) {
        Optional<TopicEntity> existing = topicRepository.findById(topicId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TopicEntity toUpdate = existing.get();
        String name = body.get("name") != null ? body.get("name") : body.get("title");
        if (name != null) toUpdate.setTitle(name);
        if (body.get("description") != null) toUpdate.setDescription(body.get("description"));

        return ResponseEntity.ok(topicRepository.save(toUpdate));
    }

    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<?> deleteTopic(@PathVariable Long topicId) {
        if (!topicRepository.existsById(topicId)) {
            return ResponseEntity.notFound().build();
        }

        // Delete associated questions first
        Optional<TopicEntity> topic = topicRepository.findById(topicId);
        if (topic.isPresent()) {
            List<Question> questions = questionRepository.findByTopicIgnoreCase(topic.get().getTitle());
            questionRepository.deleteAll(questions);
        }

        topicRepository.deleteById(topicId);
        return ResponseEntity.ok(Map.of("message", "Topic deleted successfully"));
    }

    // ============ QUESTION MANAGEMENT ============
    @GetMapping("/questions")
    public ResponseEntity<List<Map<String, Object>>> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();
        List<Map<String, Object>> questionList = questions.stream().map(q -> {
            Map<String, Object> qMap = new HashMap<>();
            qMap.put("id", q.getId());
            qMap.put("questionText", q.getQuestionText());
            qMap.put("topic", q.getTopic());
            qMap.put("difficulty", q.getDifficulty());
            qMap.put("options", List.of(
                    q.getOptionA() != null ? q.getOptionA() : "",
                    q.getOptionB() != null ? q.getOptionB() : "",
                    q.getOptionC() != null ? q.getOptionC() : "",
                    q.getOptionD() != null ? q.getOptionD() : ""
            ));
            qMap.put("correctAnswer", q.getCorrectAnswer());
            return qMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(questionList);
    }

    @GetMapping("/topics/{topicId}/questions")
    public ResponseEntity<List<Question>> getQuestionsByTopic(@PathVariable Long topicId) {
        Optional<TopicEntity> topic = topicRepository.findById(topicId);
        if (topic.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Question> questions = questionRepository.findByTopicIgnoreCase(topic.get().getTitle());
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/questions")
    public ResponseEntity<?> addQuestion(@RequestBody Map<String, Object> body) {
        Question question = new Question();
        question.setTopic((String) body.get("topic"));
        question.setQuestionText((String) body.get("questionText"));
        question.setDifficulty((String) body.getOrDefault("difficulty", "MEDIUM"));

        @SuppressWarnings("unchecked")
        List<String> options = (List<String>) body.get("options");
        if (options != null && options.size() >= 4) {
            question.setOptionA(options.get(0));
            question.setOptionB(options.get(1));
            question.setOptionC(options.get(2));
            question.setOptionD(options.get(3));
        }

        Object correctAnswerObj = body.get("correctAnswer");
        if (correctAnswerObj instanceof Integer && options != null) {
            int index = (Integer) correctAnswerObj;
            if (index >= 0 && index < options.size()) {
                question.setCorrectAnswer(options.get(index));
            }
        } else if (correctAnswerObj instanceof String) {
            question.setCorrectAnswer((String) correctAnswerObj);
        }

        Question saved = questionRepository.save(question);
        log.info("Added new question for topic: {}", question.getTopic());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/topics/{topicId}/questions")
    public ResponseEntity<?> addQuestionToTopic(@PathVariable Long topicId, @RequestBody Map<String, Object> body) {
        Optional<TopicEntity> topic = topicRepository.findById(topicId);
        if (topic.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        body.put("topic", topic.get().getTitle());
        return addQuestion(body);
    }

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long questionId, @RequestBody Map<String, Object> body) {
        Optional<Question> existing = questionRepository.findById(questionId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Question toUpdate = existing.get();

        if (body.get("questionText") != null) {
            toUpdate.setQuestionText((String) body.get("questionText"));
        }
        if (body.get("difficulty") != null) {
            toUpdate.setDifficulty((String) body.get("difficulty"));
        }

        @SuppressWarnings("unchecked")
        List<String> options = (List<String>) body.get("options");
        if (options != null && options.size() >= 4) {
            toUpdate.setOptionA(options.get(0));
            toUpdate.setOptionB(options.get(1));
            toUpdate.setOptionC(options.get(2));
            toUpdate.setOptionD(options.get(3));
        }

        Object correctAnswerObj = body.get("correctAnswer");
        if (correctAnswerObj instanceof Integer && options != null) {
            int index = (Integer) correctAnswerObj;
            if (index >= 0 && index < options.size()) {
                toUpdate.setCorrectAnswer(options.get(index));
            }
        } else if (correctAnswerObj instanceof String) {
            toUpdate.setCorrectAnswer((String) correctAnswerObj);
        }

        return ResponseEntity.ok(questionRepository.save(toUpdate));
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            return ResponseEntity.notFound().build();
        }

        questionRepository.deleteById(questionId);
        return ResponseEntity.ok(Map.of("message", "Question deleted successfully"));
    }

    // ============ AI QUESTION GENERATION ============
    @PostMapping("/topics/{topicId}/generate-questions")
    public ResponseEntity<?> generateQuestionsForTopic(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "5") int count) {

        Optional<TopicEntity> topicOpt = topicRepository.findById(topicId);
        if (topicOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String topicName = topicOpt.get().getTitle();
        log.info("Generating {} AI questions for topic: {}", count, topicName);

        try {
            String jsonResponse = aiContentService.generateQuestionsJson(topicName);

            // Parse the JSON response
            List<Map<String, Object>> questionsData = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            List<Question> savedQuestions = new ArrayList<>();

            for (Map<String, Object> qData : questionsData) {
                Question question = new Question();
                question.setTopic(topicName);
                question.setQuestionText((String) qData.get("questionText"));
                question.setDifficulty((String) qData.getOrDefault("difficulty", "MEDIUM"));

                @SuppressWarnings("unchecked")
                List<String> options = (List<String>) qData.get("options");
                if (options != null && options.size() >= 4) {
                    question.setOptionA(options.get(0));
                    question.setOptionB(options.get(1));
                    question.setOptionC(options.get(2));
                    question.setOptionD(options.get(3));
                }

                question.setCorrectAnswer((String) qData.get("correctAnswer"));

                @SuppressWarnings("unchecked")
                List<String> skillsTested = (List<String>) qData.get("skillsTested");
                if (skillsTested != null) {
                    question.setSkillsTested(String.join(",", skillsTested));
                }

                savedQuestions.add(questionRepository.save(question));
            }

            log.info("Generated and saved {} questions for topic: {}", savedQuestions.size(), topicName);
            return ResponseEntity.ok(Map.of(
                    "message", "Questions generated successfully",
                    "count", savedQuestions.size(),
                    "questions", savedQuestions
            ));

        } catch (Exception e) {
            log.error("Error generating questions: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to generate questions: " + e.getMessage()));
        }
    }
}

