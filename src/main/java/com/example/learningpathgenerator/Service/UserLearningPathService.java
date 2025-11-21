package com.example.learningpathgenerator.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.example.learningpathgenerator.entity.UserLearningPath;
import com.example.learningpathgenerator.model.LearningPath;
import com.example.learningpathgenerator.repository.UserLearningPathRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class UserLearningPathService {

    private final UserLearningPathRepository userLearningPathRepository;
    private final ObjectMapper objectMapper;

    public UserLearningPathService(UserLearningPathRepository userLearningPathRepository) {
        this.userLearningPathRepository = userLearningPathRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public Optional<LearningPath> getUserLearningPath(String username) {
        log.info("Fetching learning path for user: {}", username);

        return userLearningPathRepository.findByUsername(username)
                .map(ulp -> {
                    try {
                        return objectMapper.readValue(ulp.getLearningPathJson(), LearningPath.class);
                    } catch (Exception e) {
                        log.error("Failed to parse learning path for user: {}", username, e);
                        throw new RuntimeException("Failed to parse learning path", e);
                    }
                });
    }

    @Transactional
    public void saveLearningPath(String username, LearningPath learningPath) {
        log.info("Saving learning path for user: {}", username);

        try {
            String json = objectMapper.writeValueAsString(learningPath);

            Optional<UserLearningPath> existing = userLearningPathRepository.findByUsername(username);

            if (existing.isPresent()) {
                UserLearningPath existingPath = existing.get();
                existingPath.setLearningPathJson(json);
                userLearningPathRepository.save(existingPath);
            } else {
                UserLearningPath newPath = new UserLearningPath();
                newPath.setUsername(username);
                newPath.setLearningPathJson(json);
                userLearningPathRepository.save(newPath);
            }

            log.info("Successfully saved learning path for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to save learning path for user: {}", username, e);
            throw new RuntimeException("Failed to save learning path", e);
        }
    }

    @Transactional
    public void deleteLearningPath(String username) {
        log.info("Deleting learning path for user: {}", username);
        userLearningPathRepository.deleteByUsername(username);
    }

    public boolean hasLearningPath(String username) {
        return userLearningPathRepository.existsByUsername(username);
    }
}