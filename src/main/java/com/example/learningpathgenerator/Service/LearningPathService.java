package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.dto.GenerateRequest;
import com.example.learningpathgenerator.model.LearningPath;
import com.example.learningpathgenerator.model.module;
import com.example.learningpathgenerator.model.Topic;
import com.example.learningpathgenerator.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningPathService {

    private final TopicRepository topicRepository;

    public LearningPathService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public LearningPath generatePath(GenerateRequest request) {
        List<Topic> all = topicRepository.findAll();

        String skill = Optional.ofNullable(request.getSkillLevel()).orElse("BEGINNER").toUpperCase(Locale.ROOT);
        Set<String> interests = Optional.ofNullable(request.getInterests())
                .map(list -> list.stream().map(String::toLowerCase).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        List<Topic> candidates = new ArrayList<>(all);

        Map<Topic, Double> scores = new HashMap<>();
        int userSkillNumeric = skillToNumeric(skill);

        for (Topic t : candidates) {
            double score = 0.0;
            String cat = Optional.ofNullable(t.getCategory()).orElse("").toLowerCase(Locale.ROOT);
            String name = Optional.ofNullable(t.getName()).orElse("").toLowerCase(Locale.ROOT);

            for (String interest : interests) {
                if (cat.contains(interest) || name.contains(interest) || interest.contains(cat) || t.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase(interest))) {
                    score += 3.0;
                }
            }

            int gap = Math.abs(t.getDifficulty() - userSkillNumeric);
            score += Math.max(0, 5 - gap);

            if ("core".equalsIgnoreCase(t.getCategory())) score += 1.0;

            if (request.getTarget() != null && request.getTarget().toLowerCase().contains("web") && "web".equalsIgnoreCase(t.getCategory())) {
                score += 1.5;
            }

            scores.put(t, score);
        }

        int maxTopics = Math.min(candidates.size(), 6 + (3 - userSkillNumeric));
        List<Topic> picked = scores.entrySet().stream()
                .sorted(Map.Entry.<Topic, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(maxTopics)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (userSkillNumeric <= 2) {
            ensureTopicByIdOrName(picked, all, "t-java-basics");
            ensureTopicByIdOrName(picked, all, "t-oop");
        }

        List<module> modules = picked.stream()
                .map(t -> new module(
                        t.getName(),
                        "Learn " + t.getName() + " (" + t.getCategory() + "). Difficulty: " + t.getDifficulty(),
                        t.getRecommendedHours(),
                        t.getResources()

                ))
                .collect(Collectors.toList());

        int totalHours = modules.stream().mapToInt(module::getHours).sum();
        int weekly = Math.max(1, request.getWeeklyHours());
        int estimatedWeeks = (int) Math.ceil((double) totalHours / weekly);

        String title = generateTitle(request.getTarget(), request.getName(), skill);

        return new LearningPath(title,
                Optional.ofNullable(request.getName()).orElse("Learner"),
                skill,
                weekly,
                estimatedWeeks,
                totalHours,
                modules);
    }

    private void ensureTopicByIdOrName(List<Topic> picked, List<Topic> all, String idOrName) {
        boolean present = picked.stream().anyMatch(t -> t.getId().equalsIgnoreCase(idOrName) || t.getName().toLowerCase().contains(idOrName.toLowerCase()));
        if (!present) {
            for (Topic t : all) {
                if (t.getId().equalsIgnoreCase(idOrName) || t.getName().toLowerCase().contains(idOrName.toLowerCase())) {
                    picked.add(0, t);
                    break;
                }
            }
        }
    }

    private String generateTitle(String target, String name, String skill) {
        String who = (name == null || name.isBlank()) ? "Personalized" : name + "'s";
        String what = (target == null || target.isBlank()) ? "Learning Path" : target + " Learning Path";
        return who + " " + what + " (" + skill + ")";
    }

    private int skillToNumeric(String skill) {
        switch (skill.toUpperCase(Locale.ROOT)) {
            case "BEGINNER":
                return 1;
            case "INTERMEDIATE":
                return 3;
            case "ADVANCED":
                return 5;
            default:
                try {
                    int v = Integer.parseInt(skill);
                    return Math.max(1, Math.min(5, v));
                } catch (Exception e) {
                    return 2;
                }
        }
    }
}