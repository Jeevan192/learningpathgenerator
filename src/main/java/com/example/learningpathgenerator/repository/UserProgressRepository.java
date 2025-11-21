package com.example.learningpathgenerator.repository;

import com.example.learningpathgenerator.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findByUsername(String username);
    boolean existsByUsername(String username);
    void deleteByUsername(String username);
}