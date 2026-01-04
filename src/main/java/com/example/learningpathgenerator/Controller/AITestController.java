package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.Service.AIContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AITestController {

    private final AIContentService aiContentService;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @GetMapping("/status")
    public ResponseEntity<?> getAIStatus() {
        boolean configured = apiKey != null && !apiKey.isBlank() && !apiKey.equals("your-api-key-here");
        return ResponseEntity.ok(Map.of(
                "aiConfigured", configured,
                "keyPresent", apiKey != null && !apiKey.isBlank(),
                "keyLength", apiKey != null ? apiKey.length() : 0
        ));
    }

    @GetMapping("/generate-questions/{topic}")
    public ResponseEntity<String> testGenerateQuestions(@PathVariable String topic) {
        log.info("Testing question generation for topic: {}", topic);
        String questions = aiContentService.generateQuestionsJson(topic);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/generate-path/{topic}")
    public ResponseEntity<String> testGeneratePath(@PathVariable String topic) {
        log.info("Testing learning path generation for topic: {}", topic);
        List<String> weaknesses = List.of("variables", "loops", "functions");
        String path = aiContentService.generateLearningPathJson(topic, weaknesses);
        return ResponseEntity.ok(path);
    }

    @GetMapping("/generate-description/{skill}")
    public ResponseEntity<String> testGenerateDescription(@PathVariable String skill) {
        log.info("Testing description generation for skill: {}", skill);
        String description = aiContentService.generateLearningPathDescription(
                skill,
                List.of("error handling", "debugging"),
                List.of("basic syntax", "variables"),
                "HANDS_ON"
        );
        return ResponseEntity.ok(description);
    }
}

