package com.example.learningpathgenerator.repository;

import com.example.learningpathgenerator.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTopicIgnoreCase(String topic);

    @Query("SELECT DISTINCT q.topic FROM Question q")
    List<String> findDistinctTopics();
}
