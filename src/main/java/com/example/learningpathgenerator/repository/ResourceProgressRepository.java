package com.example.learningpathgenerator.repository;

import com.example.learningpathgenerator.entity.LearningResource;
import com.example.learningpathgenerator.entity.ResourceProgress;
import com.example.learningpathgenerator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceProgressRepository extends JpaRepository<ResourceProgress, Long> {
    List<ResourceProgress> findByUser(User user);
    Optional<ResourceProgress> findByUserAndResource(User user, LearningResource resource);
    List<ResourceProgress> findByUserAndStatus(User user, String status);
}