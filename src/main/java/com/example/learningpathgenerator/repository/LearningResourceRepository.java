package com.example.learningpathgenerator.repository;

import com.example.learningpathgenerator.entity.LearningResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResource, Long> {
    List<LearningResource> findByDifficultyLevel(String difficultyLevel);
    List<LearningResource> findByResourceType(String resourceType);
}