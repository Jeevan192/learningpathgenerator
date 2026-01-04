package com.example.learningpathgenerator.repository;

import com.example.learningpathgenerator.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByTopicAndUserId(String topic, Long userId);
}
