package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.dto.QuizAnalysisResult;
import com.example.learningpathgenerator.dto.QuizSubmission;
import com.example.learningpathgenerator.dto.SkillProfile;
import com.example.learningpathgenerator.entity.*;
import com.example.learningpathgenerator.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    // Use gemini-2.5-flash - same model as AIContentService
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    @Override
    public List<String> availableTopicIds() {
        List<String> topics = questionRepository.findDistinctTopics();
        return topics.isEmpty() ? List.of("Java", "Python", "Spring Boot", "React") : topics;
    }

    @Override
    public List<Question> getQuestionsByTopic(String topic) {
        List<Question> questions = questionRepository.findByTopicIgnoreCase(topic);
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }
        Collections.shuffle(questions);
        return questions.size() > 10 ? questions.subList(0, 10) : questions;
    }

    @Override
    public Quiz getQuizForUser(String topic, Long userId) {
        List<Question> questions = getQuestionsByTopic(topic);

        Quiz quiz = new Quiz();
        quiz.setTitle(topic + " Assessment");
        quiz.setTopic(topic);
        quiz.setUserId(userId);
        quiz.setQuestions(questions);
        quiz.setCreatedAt(new Date());
        quiz.setPassingScore(Math.max(1, (int)(questions.size() * 0.6)));

        return quizRepository.save(quiz);
    }

    @Override
    public QuizAnalysisResult submitQuiz(QuizSubmission submission, Long userId) {
        int correctCount = 0;
        int totalQuestions = 0;
        List<Map<String, Object>> detailedAnswers = new ArrayList<>();
        Map<Long, Integer> confidenceMap = submission.getConfidence() != null ? submission.getConfidence() : new HashMap<>();

        if (submission.getAnswers() != null) {
            totalQuestions = submission.getAnswers().size();
            for (Map.Entry<Long, String> entry : submission.getAnswers().entrySet()) {
                Long qId = entry.getKey();
                String answer = entry.getValue();

                Optional<Question> qOpt = questionRepository.findById(qId);
                if (qOpt.isPresent()) {
                    Question q = qOpt.get();
                    boolean isCorrect = q.getCorrectAnswer().equals(answer);
                    if (isCorrect) correctCount++;

                    Map<String, Object> detail = new HashMap<>();
                    detail.put("question", q.getQuestionText());
                    detail.put("userAnswer", answer);
                    detail.put("correctAnswer", q.getCorrectAnswer());
                    detail.put("isCorrect", isCorrect);
                    detail.put("difficulty", q.getDifficulty());
                    // Add confidence to the analysis data
                    detail.put("confidence", confidenceMap.getOrDefault(qId, 0));
                    detailedAnswers.add(detail);
                }
            }
        }

        double scorePercentage = totalQuestions > 0 ? ((double) correctCount / totalQuestions) * 100 : 0;

        // Send detailed answers + confidence to AI
        SkillProfile aiGeneratedProfile = analyzePerformanceWithAI(submission.getTopic(), detailedAnswers, scorePercentage);

        return new QuizAnalysisResult(
                scorePercentage,
                correctCount,
                totalQuestions,
                aiGeneratedProfile.getCurrentLevel(),
                "AI Analysis Complete",
                null,
                aiGeneratedProfile
        );
    }

    @Override
    public Question addQuestion(Question question) {
        return questionRepository.save(question);
    }

    private SkillProfile analyzePerformanceWithAI(String topic, List<Map<String, Object>> detailedAnswers, double score) {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.out.println("AI Key Missing - using fallback profile");
            return createFallbackProfile(topic, score);
        }

        try {
            // Updated prompt to explicitly use confidence levels
            String prompt = String.format("""
                Analyze this quiz performance for topic: %s. Score: %.2f%%.
                
                Detailed Answers (includes 'confidence' 0-100): 
                %s
                
                Analysis Logic:
                1. High Confidence (>70) + Wrong Answer = Critical Weakness (Misconception).
                2. Low Confidence (<40) + Correct Answer = Lucky Guess (Needs Reinforcement).
                3. High Confidence + Correct Answer = Strength.
                
                Return ONLY a valid JSON object:
                { 
                  "currentLevel": "BEGINNER" or "INTERMEDIATE" or "ADVANCED", 
                  "strengths": ["List of specific concepts"], 
                  "weaknesses": ["List of specific weak concepts"] 
                }
                """, topic, score, objectMapper.writeValueAsString(detailedAnswers));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(GEMINI_API_URL + "?key=" + geminiApiKey, HttpMethod.POST, entity, Map.class);
            return parseAIAnalysis(response.getBody(), topic);
        } catch (Exception e) {
            System.err.println("AI Analysis failed: " + e.getMessage());
            // Return a complete fallback profile with all required fields
            return createFallbackProfile(topic, score);
        }
    }

    private SkillProfile createFallbackProfile(String topic, double score) {
        SkillProfile profile = new SkillProfile();

        // Determine level based on score
        if (score >= 80) {
            profile.setCurrentLevel("ADVANCED");
        } else if (score >= 60) {
            profile.setCurrentLevel("INTERMEDIATE");
        } else {
            profile.setCurrentLevel("BEGINNER");
        }

        // Set default strengths and weaknesses based on score
        if (score >= 70) {
            profile.setStrengths(List.of("Core concepts", "Basic understanding"));
            profile.setWeaknesses(List.of("Advanced topics", "Edge cases"));
        } else {
            profile.setStrengths(List.of("Showing interest", "Getting started"));
            profile.setWeaknesses(List.of("Fundamentals", "Core concepts", "Practice needed"));
        }

        profile.setRecommendedLearningStyle("HANDS_ON");
        profile.setSkillProficiency(Map.of(topic, profile.getCurrentLevel()));

        return profile;
    }

    @SuppressWarnings("unchecked")
    private SkillProfile parseAIAnalysis(Map<String, Object> response, String topic) {
        SkillProfile profile = new SkillProfile();
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                String text = (String) ((List<Map<String, Object>>) ((Map<String, Object>) candidates.get(0).get("content")).get("parts")).get(0).get("text");
                text = text.replace("```json", "").replace("```", "").trim();
                int start = text.indexOf("{");
                int end = text.lastIndexOf("}");
                if (start != -1 && end != -1) {
                    String json = text.substring(start, end + 1);
                    Map<String, Object> map = objectMapper.readValue(json, Map.class);
                    profile.setCurrentLevel((String) map.getOrDefault("currentLevel", "INTERMEDIATE"));
                    profile.setStrengths((List<String>) map.getOrDefault("strengths", List.of("Basic concepts")));
                    profile.setWeaknesses((List<String>) map.getOrDefault("weaknesses", List.of("Advanced topics")));
                    profile.setRecommendedLearningStyle("HANDS_ON");
                    profile.setSkillProficiency(Map.of(topic, profile.getCurrentLevel()));
                    return profile;
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + e.getMessage());
        }
        // Fallback values
        profile.setCurrentLevel("INTERMEDIATE");
        profile.setStrengths(List.of("Core concepts"));
        profile.setWeaknesses(List.of("Advanced topics"));
        profile.setRecommendedLearningStyle("HANDS_ON");
        profile.setSkillProficiency(Map.of(topic, "INTERMEDIATE"));
        return profile;
    }
}
