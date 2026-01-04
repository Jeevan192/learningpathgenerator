package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.Service.QuizService;
import com.example.learningpathgenerator.Service.AIContentService;
import com.example.learningpathgenerator.dto.QuizAnalysisResult;
import com.example.learningpathgenerator.dto.QuizSubmission;
import com.example.learningpathgenerator.entity.Question;
import com.example.learningpathgenerator.entity.Quiz;
import com.example.learningpathgenerator.entity.TopicEntity;
import com.example.learningpathgenerator.entity.User;
import com.example.learningpathgenerator.repository.TopicRepository;
import com.example.learningpathgenerator.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class QuizController {

    private final QuizService quizService;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final AIContentService aiContentService;
    private final ObjectMapper objectMapper;

    @GetMapping("/topics")
    public ResponseEntity<List<String>> getTopics() {
        // Get topics from TopicEntity table
        List<TopicEntity> topicEntities = topicRepository.findAll();
        List<String> topics = topicEntities.stream()
                .map(TopicEntity::getTitle)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // If no topics in database, add some defaults
        if (topics.isEmpty()) {
            topics = List.of("Java", "Python", "Spring Boot", "React", "JavaScript");
        }

        return ResponseEntity.ok(topics);
    }

    @GetMapping("/questions/{topic}")
    public ResponseEntity<?> getQuestionsByTopic(@PathVariable String topic) {
        log.info("Fetching questions for topic: {}", topic);

        List<Question> questions = quizService.getQuestionsByTopic(topic);

        // If no questions in DB, generate AI questions
        if (questions.isEmpty()) {
            log.info("No questions found in DB for topic {}, generating AI questions...", topic);
            try {
                questions = generateAIQuestions(topic, 10);
            } catch (Exception e) {
                log.error("Failed to generate AI questions: {}", e.getMessage());
                return ResponseEntity.ok(Collections.emptyList());
            }
        }

        // Format questions for frontend with options as array
        List<Map<String, Object>> formattedQuestions = questions.stream().map(q -> {
            Map<String, Object> qMap = new HashMap<>();
            qMap.put("id", q.getId());
            qMap.put("questionText", q.getQuestionText());
            qMap.put("topic", q.getTopic());
            qMap.put("difficulty", q.getDifficulty());

            // Build options array
            List<String> options = new ArrayList<>();
            if (q.getOptionA() != null) options.add(q.getOptionA());
            if (q.getOptionB() != null) options.add(q.getOptionB());
            if (q.getOptionC() != null) options.add(q.getOptionC());
            if (q.getOptionD() != null) options.add(q.getOptionD());
            qMap.put("options", options);

            qMap.put("correctAnswer", q.getCorrectAnswer());
            return qMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(formattedQuestions);
    }

    @GetMapping("/start")
    public ResponseEntity<Quiz> startQuiz(@RequestParam String topic, @RequestParam Long userId) {
        return ResponseEntity.ok(quizService.getQuizForUser(topic, userId));
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizAnalysisResult> submitQuiz(@RequestBody QuizSubmission submission) {
        User user = getCurrentUser();
        Long userId = user != null ? Long.valueOf(user.getId()) : 0L;

        log.info("Quiz submitted for topic: {} by user: {}", submission.getTopic(), userId);
        log.info("Answers: {}", submission.getAnswers());
        log.info("Confidence levels: {}", submission.getConfidence());

        return ResponseEntity.ok(quizService.submitQuiz(submission, userId));
    }

    @PostMapping("/add")
    public ResponseEntity<Question> addQuestion(@RequestBody Question question) {
        return ResponseEntity.ok(quizService.addQuestion(question));
    }

    // AI Question Generation endpoint
    @PostMapping("/generate/{topic}")
    public ResponseEntity<?> generateQuestions(
            @PathVariable String topic,
            @RequestParam(defaultValue = "5") int count) {

        log.info("Generating {} AI questions for topic: {}", count, topic);

        try {
            List<Question> questions = generateAIQuestions(topic, count);
            return ResponseEntity.ok(Map.of(
                    "message", "Questions generated successfully",
                    "count", questions.size(),
                    "questions", questions
            ));
        } catch (Exception e) {
            log.error("Error generating questions: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to generate questions: " + e.getMessage()));
        }
    }

    private List<Question> generateAIQuestions(String topic, int count) throws Exception {
        String jsonResponse = aiContentService.generateQuestionsJson(topic);

        // Parse JSON response
        List<Map<String, Object>> questionsData = objectMapper.readValue(
                jsonResponse,
                new TypeReference<List<Map<String, Object>>>() {}
        );

        List<Question> savedQuestions = new ArrayList<>();

        for (Map<String, Object> qData : questionsData) {
            Question question = new Question();
            question.setTopic(topic);
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

            savedQuestions.add(quizService.addQuestion(question));
        }

        return savedQuestions;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }
}
