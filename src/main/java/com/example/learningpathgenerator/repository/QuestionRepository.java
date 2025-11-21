package com.example.learningpathgenerator.repository;

import com.example.learningpathgenerator.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    List<Question> findByTopicId(String topicId);
    void deleteByTopicId(String topicId);
}