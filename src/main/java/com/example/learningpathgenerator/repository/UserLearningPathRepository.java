package com.example.learningpathgenerator.repository;

import com.example.learningpathgenerator.entity.UserLearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLearningPathRepository extends JpaRepository<UserLearningPath, Long> {
    Optional<UserLearningPath> findByUsername(String username);
    boolean existsByUsername(String username);
    void deleteByUsername(String username);
}