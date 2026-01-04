package com.example.learningpathgenerator.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AIContentService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Using v1beta and a known working model from recent logs.
    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GEMINI_MODEL = "gemini-2.5-flash";

    private boolean isAIEnabled() {
        boolean enabled = apiKey != null && !apiKey.isBlank() && !apiKey.equals("your-api-key-here");
        log.info("AI Service enabled: {}, keyLength: {}", enabled, apiKey != null ? apiKey.length() : 0);
        return enabled;
    }

    public String generateQuestionsJson(String topic) {
        log.info("Generating questions for topic: {}", topic);

        String prompt = String.format("""
            Generate 5 quiz questions about %s in JSON format.
            Return ONLY a valid JSON array with this exact structure (no markdown, no explanation):
            [
              {
                "questionText": "question here",
                "topic": "%s",
                "options": ["Option A", "Option B", "Option C", "Option D"],
                "correctAnswer": "Option A",
                "difficulty": "MEDIUM",
                "skillsTested": ["skill1", "skill2"]
              }
            ]
            """, topic, topic);

        String response = callGemini(prompt);
        log.info("Generated questions response length: {}", response.length());
        return response;
    }

    public String generateLearningPathJson(String topic, List<String> weaknesses) {
        log.info("Generating learning path for topic: {} with weaknesses: {}", topic, weaknesses);

        String prompt = String.format("""
            Create a learning path for %s focusing on these weak areas: %s.
            Return ONLY a valid JSON array with this exact structure (no markdown, no explanation):
            [
              {
                "title": "Resource Title",
                "description": "Description",
                "url": "https://example.com",
                "resourceType": "VIDEO",
                "estimatedDuration": 30,
                "difficulty": "BEGINNER"
              }
            ]
            """, topic, String.join(", ", weaknesses));

        return callGemini(prompt);
    }

    public String analyzeQuizPerformance(String quizContextJson) {
        log.info("Analyzing quiz performance with AI");

        String prompt = String.format("""
            Analyze this quiz performance data and provide detailed feedback.
            Quiz Data: %s

            Return ONLY a valid JSON object with this exact structure (no markdown):
            {
              "scorePercentage": 0,
              "correctCount": 0,
              "totalQuestions": 0,
              "performance": "GOOD",
              "feedback": "Detailed personalized feedback about strengths and areas for improvement",
              "learningPathId": null,
              "skillProfile": {
                "currentLevel": "INTERMEDIATE",
                "strengths": ["skill1"],
                "weaknesses": ["skill2"],
                "recommendedLearningStyle": "HANDS_ON",
                "skillProficiency": {"topic": "INTERMEDIATE"}
              }
            }
            """, quizContextJson);

        return callGemini(prompt);
    }

    public String generateLearningPathDescription(String targetSkill,
                                                  List<String> weaknesses,
                                                  List<String> strengths,
                                                  String learningStyle) {
        log.info("Generating learning path description for skill: {}", targetSkill);

        String prompt = String.format("""
            Generate a brief, motivating description for a learning path:
            - Target Skill: %s
            - Areas to improve: %s
            - Current strengths: %s
            - Learning style: %s

            Return ONLY a 2-3 sentence encouraging description. No JSON, just plain text.
            """,
                targetSkill,
                weaknesses != null && !weaknesses.isEmpty() ? String.join(", ", weaknesses) : "general concepts",
                strengths != null && !strengths.isEmpty() ? String.join(", ", strengths) : "foundational knowledge",
                learningStyle != null ? learningStyle : "mixed"
        );

        return callGemini(prompt);
    }

    private String callGemini(String prompt) {
        if (!isAIEnabled()) {
            log.warn("AI not enabled - using mock response");
            return getMockResponse(prompt);
        }

        try {
            log.info("Calling Gemini API with model: {}", GEMINI_MODEL);

            String baseUrl = GEMINI_API_BASE_URL + GEMINI_MODEL + ":generateContent";
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("key", apiKey)
                    .toUriString();

            log.debug("Request URL (key redacted): {}?key=<REDACTED>", baseUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                    Map.of("parts", List.of(Map.of("text", prompt)))
            ));
            // Add generationConfig for safety
            requestBody.put("generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 2048
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.debug("Sending request to Gemini API...");
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("Received response with status: {}", response.getStatusCode());

                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        String text = (String) parts.get(0).get("text");
                        log.info("Gemini response received successfully, length: {}", text.length());
                        return cleanJsonResponse(text);
                    }
                }
                log.warn("Gemini response was OK but malformed or empty.");
            } else {
                log.error("Gemini returned non-OK status: {}", response.getStatusCode());
            }

            return getMockResponse(prompt);

        } catch (HttpClientErrorException e) {
            log.error("Gemini API HTTP error - Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return getMockResponse(prompt);
        } catch (Exception e) {
            log.error("Gemini API call failed with unexpected error: {}", e.getMessage(), e);
            return getMockResponse(prompt);
        }
    }

    private String cleanJsonResponse(String response) {
        String cleaned = response.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }

    private String getMockResponse(String prompt) {
        log.info("Returning mock response for prompt type");

        if (prompt.contains("quiz questions") || prompt.contains("Generate 5")) {
            return """
                [
                  {"questionText":"What is the main purpose of Java?","topic":"Java","options":["Web Development","General Purpose Programming","Database Management","Operating System"],"correctAnswer":"General Purpose Programming","difficulty":"EASY","skillsTested":["basics","fundamentals"]},
                  {"questionText":"What is JVM?","topic":"Java","options":["Java Virtual Machine","Java Visual Machine","Java Variable Method","Java Verified Module"],"correctAnswer":"Java Virtual Machine","difficulty":"EASY","skillsTested":["basics","architecture"]},
                  {"questionText":"Which keyword is used to inherit a class?","topic":"Java","options":["implements","extends","inherits","super"],"correctAnswer":"extends","difficulty":"MEDIUM","skillsTested":["OOP","inheritance"]},
                  {"questionText":"What is encapsulation?","topic":"Java","options":["Hiding implementation details","Creating objects","Inheriting properties","Method overloading"],"correctAnswer":"Hiding implementation details","difficulty":"MEDIUM","skillsTested":["OOP","encapsulation"]},
                  {"questionText":"What is polymorphism?","topic":"Java","options":["One interface, multiple implementations","Data hiding","Code reuse","Memory management"],"correctAnswer":"One interface, multiple implementations","difficulty":"HARD","skillsTested":["OOP","polymorphism"]}
                ]
                """;
        } else if (prompt.contains("learning path") && prompt.contains("JSON")) {
            return """
                [
                  {"title":"Introduction to Programming Concepts","description":"Learn fundamental programming concepts","url":"https://example.com/intro","resourceType":"VIDEO","estimatedDuration":30,"difficulty":"BEGINNER"},
                  {"title":"Hands-on Practice Exercises","description":"Apply what you learned with exercises","url":"https://example.com/practice","resourceType":"PRACTICE","estimatedDuration":45,"difficulty":"BEGINNER"},
                  {"title":"Advanced Topics Deep Dive","description":"Explore advanced concepts","url":"https://example.com/advanced","resourceType":"ARTICLE","estimatedDuration":60,"difficulty":"INTERMEDIATE"}
                ]
                """;
        } else if (prompt.contains("description") || prompt.contains("motivating")) {
            return "This personalized learning path is designed to strengthen your skills and address your specific learning needs. Through carefully curated resources, you'll build confidence and master key concepts at your own pace.";
        } else if (prompt.contains("Analyze") || prompt.contains("performance")) {
            return """
                {"scorePercentage":75.0,"correctCount":3,"totalQuestions":4,"performance":"GOOD","feedback":"Great effort! You demonstrated solid understanding of core concepts. Focus on strengthening your knowledge in advanced topics to reach the next level.","learningPathId":null,"skillProfile":{"currentLevel":"INTERMEDIATE","strengths":["basics","fundamentals"],"weaknesses":["advanced concepts","edge cases"],"recommendedLearningStyle":"HANDS_ON","skillProficiency":{"Java":"INTERMEDIATE"}}}
                """;
        }

        return "{}";
    }
}
