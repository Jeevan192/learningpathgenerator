package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.model.Topic;
import com.example.learningpathgenerator.model.Question;
import com.example.learningpathgenerator.model.Quiz;

import java.util.*;

public interface QuizService {
    Set<String> availableTopicIds();
    Optional<Quiz> findByTopicId(String topicId);
    List<Topic> getAllTopics();

    // ADMIN methods
    void addTopic(Topic topic);
    void updateTopic(String topicId, Topic topic);
    void deleteTopic(String topicId);
    void addQuestionToTopic(String topicId, Question question);
    void updateQuestion(String questionId, Question question);
    void deleteQuestion(String questionId);
    List<Question> getQuestionsByTopicId(String topicId);
}