package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.dto.QuizAnalysisResult;
import com.example.learningpathgenerator.dto.QuizSubmission;
import com.example.learningpathgenerator.entity.Question;
import com.example.learningpathgenerator.entity.Quiz;

import java.util.List;

public interface QuizService {
    List<String> availableTopicIds();
    List<Question> getQuestionsByTopic(String topic);
    Quiz getQuizForUser(String topic, Long userId);
    QuizAnalysisResult submitQuiz(QuizSubmission submission, Long userId);
    Question addQuestion(Question question);
}
