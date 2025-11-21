package com.example.learningpathgenerator.Service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIContentService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private OpenAiService openAiService;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-key-here")) {
            try {
                this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
                log.info("OpenAI service initialized successfully");
            } catch (Exception e) {
                log.warn("Failed to initialize OpenAI service: {}", e.getMessage());
            }
        } else {
            log.warn("OpenAI API key not configured. AI features will use fallback responses.");
        }
    }

    public String generateLearningPathDescription(
            String targetSkill,
            List<String> weaknesses,
            List<String> strengths,
            String learningStyle) {

        if (openAiService == null) {
            return generateFallbackDescription(targetSkill, weaknesses, strengths, learningStyle);
        }

        String prompt = String.format("""
            Create a motivating and personalized learning path description for a student with:
            - Target Skill: %s
            - Weaknesses: %s
            - Strengths: %s
            - Learning Style: %s
            
            The description should be encouraging, specific, and highlight how this path will help them improve.
            Keep it concise (3-4 sentences).
            """, targetSkill,
                weaknesses != null ? String.join(", ", weaknesses) : "None",
                strengths != null ? String.join(", ", strengths) : "None",
                learningStyle);

        try {
            return callAI(prompt);
        } catch (Exception e) {
            log.error("Error calling AI service: {}", e.getMessage());
            return generateFallbackDescription(targetSkill, weaknesses, strengths, learningStyle);
        }
    }

    public List<String> generateStudyTips(
            List<String> weaknesses,
            String learningStyle,
            int currentStreak) {

        if (openAiService == null) {
            return generateFallbackTips(weaknesses, learningStyle, currentStreak);
        }

        String prompt = String.format("""
            Generate 5 specific, actionable study tips for a student with:
            - Weaknesses in: %s
            - Learning style: %s
            - Current study streak: %d days
            
            Make tips practical, encouraging, and tailored to their profile.
            Return as a numbered list.
            """,
                weaknesses != null ? String.join(", ", weaknesses) : "general topics",
                learningStyle,
                currentStreak);

        try {
            String response = callAI(prompt);
            return parseListResponse(response);
        } catch (Exception e) {
            log.error("Error generating study tips: {}", e.getMessage());
            return generateFallbackTips(weaknesses, learningStyle, currentStreak);
        }
    }

    public String explainAnswer(String question, String correctAnswer, String userAnswer) {
        if (openAiService == null) {
            return generateFallbackExplanation(question, correctAnswer, userAnswer);
        }

        String prompt = String.format("""
            Question: %s
            Correct Answer: %s
            User's Answer: %s
            
            Provide a clear, educational explanation of:
            1. Why the correct answer is right
            2. Why the user's answer was incorrect (if different)
            3. Key concept to remember
            
            Be encouraging and focus on learning, not mistakes.
            """, question, correctAnswer, userAnswer);

        try {
            return callAI(prompt);
        } catch (Exception e) {
            log.error("Error explaining answer: {}", e.getMessage());
            return generateFallbackExplanation(question, correctAnswer, userAnswer);
        }
    }

    public String generateAchievementMessage(String achievementName, Map<String, Object> stats) {
        if (openAiService == null) {
            return generateFallbackAchievement(achievementName, stats);
        }

        String prompt = String.format("""
            User just earned the achievement: "%s"
            Their stats: %s
            
            Create a short, exciting congratulations message (2-3 sentences) that:
            - Celebrates their accomplishment
            - Mentions specific stats if impressive
            - Encourages continued learning
            
            Be enthusiastic but not over-the-top.
            """, achievementName, stats.toString());

        try {
            return callAI(prompt);
        } catch (Exception e) {
            log.error("Error generating achievement message: {}", e.getMessage());
            return generateFallbackAchievement(achievementName, stats);
        }
    }

    private String callAI(String promptText) {
        if (openAiService == null) {
            throw new RuntimeException("OpenAI service not initialized");
        }

        try {
            ChatMessage message = new ChatMessage("user", promptText);

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(List.of(message))
                    .temperature(0.7)
                    .maxTokens(500)
                    .build();

            var response = openAiService.createChatCompletion(request);
            return response.getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage());
            throw e;
        }
    }

    private List<String> parseListResponse(String response) {
        return List.of(response.split("\n")).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // Fallback methods when AI is not available
    private String generateFallbackDescription(String targetSkill, List<String> weaknesses, List<String> strengths, String learningStyle) {
        StringBuilder desc = new StringBuilder();
        desc.append("This personalized learning path is designed to help you master ").append(targetSkill).append(". ");

        if (weaknesses != null && !weaknesses.isEmpty()) {
            desc.append("We'll focus on strengthening your skills in ")
                    .append(String.join(", ", weaknesses)).append(". ");
        }

        if (strengths != null && !strengths.isEmpty()) {
            desc.append("Building on your existing knowledge of ")
                    .append(String.join(", ", strengths)).append(", ");
        }

        desc.append("this path uses a ").append(learningStyle != null ? learningStyle.toLowerCase() : "mixed")
                .append(" learning approach to maximize your understanding and retention.");

        return desc.toString();
    }

    private List<String> generateFallbackTips(List<String> weaknesses, String learningStyle, int currentStreak) {
        List<String> tips = List.of(
                "1. Practice coding every day to build consistency and improve your skills.",
                "2. Break down complex problems into smaller, manageable parts.",
                "3. Review and understand your mistakes - they're valuable learning opportunities.",
                "4. Build small projects to apply what you've learned in practical scenarios.",
                "5. Join online communities to learn from others and share your knowledge."
        );

        if (currentStreak > 0) {
            tips = new java.util.ArrayList<>(tips);
            tips.add("Great job maintaining a " + currentStreak + " day streak! Keep it up!");
        }

        return tips;
    }

    private String generateFallbackExplanation(String question, String correctAnswer, String userAnswer) {
        if (correctAnswer.equals(userAnswer)) {
            return "Correct! You got this one right. Keep up the great work!";
        }

        return String.format(
                "The correct answer is '%s'. Your answer '%s' was incorrect. " +
                        "Review the concept and try similar questions to strengthen your understanding. " +
                        "Remember, mistakes are part of the learning process!",
                correctAnswer, userAnswer
        );
    }

    private String generateFallbackAchievement(String achievementName, Map<String, Object> stats) {
        return String.format(
                "🎉 Congratulations! You've earned the '%s' achievement! " +
                        "Your dedication to learning is paying off. Keep pushing forward!",
                achievementName
        );
    }
}