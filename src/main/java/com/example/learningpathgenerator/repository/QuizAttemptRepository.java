package com.example.learningpathgenerator.repository;

import com.example.learningpathgenerator.entity.QuizAttempt;
import com.example.learningpathgenerator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserOrderByCompletedAtDesc(User user);
}
