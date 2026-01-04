package com.example.learningpathgenerator.repository;

import com.example.learningpathgenerator.entity.ResourceProgress;
import com.example.learningpathgenerator.entity.LearningResource;
import com.example.learningpathgenerator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourceProgressRepository extends JpaRepository<ResourceProgress, Long> {
    Optional<ResourceProgress> findByUserAndResource(User user, LearningResource resource);
}