package com.example.learningpathgenerator.repository;

import com.example.learningpathgenerator.entity.LearningPath;
import com.example.learningpathgenerator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
    List<LearningPath> findByUser(User user);
    List<LearningPath> findByUserAndStatus(User user, String status);
}
