package com.example.learningpathgenerator;

import com.example.learningpathgenerator.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private LearningResourceRepository resourceRepository;

    @Test
    void contextLoads() {
        assertTrue(true);
    }

    @Test
    void testDataInitialized() {
        assertTrue(userRepository.count() > 0, "Users should be initialized");
        assertTrue(quizRepository.count() > 0, "Quizzes should be initialized");
        assertTrue(resourceRepository.count() > 0, "Resources should be initialized");
    }
}