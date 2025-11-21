package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.entity.QuestionEntity;
import com.example.learningpathgenerator.entity.TopicEntity;
import com.example.learningpathgenerator.model.Topic;
import com.example.learningpathgenerator.model.Question;
import com.example.learningpathgenerator.model.Quiz;
import com.example.learningpathgenerator.repository.QuestionRepository;
import com.example.learningpathgenerator.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    public QuizServiceImpl(TopicRepository topicRepository, QuestionRepository questionRepository) {
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public Set<String> availableTopicIds() {
        return topicRepository.findAll().stream()
                .map(TopicEntity::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<Quiz> findByTopicId(String topicId) {
        Optional<TopicEntity> topicOpt = topicRepository.findById(topicId);
        if (topicOpt.isEmpty()) {
            return Optional.empty();
        }

        TopicEntity topic = topicOpt.get();
        List<Question> questionEntities = questionRepository.findByTopicId(topicId);

        List<QuestionEntity> questions = questionEntities.stream()
                .map(qe -> new Question(qe.getId(), qe.getText(), qe.getOptions(), qe.getCorrectIndex()))
                .collect(Collectors.toList());

        Quiz quiz = new Quiz(topic.getId(), topic.getName(), questions);
        return Optional.of(quiz);
    }

    @Override
    public List<Topic> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(te -> new Topic(te.getId(), te.getName(), te.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addTopic(Topic topic) {
        if (topic.getId() == null || topic.getId().isEmpty()) {
            topic.setId(topic.getName().toLowerCase().replace(" ", "-"));
        }

        TopicEntity entity = new TopicEntity(topic.getId(), topic.getName(), topic.getDescription());
        topicRepository.save(entity);
        System.out.println("✅ Saved topic to database: " + topic.getName());
    }

    @Override
    @Transactional
    public void updateTopic(String topicId, Topic topic) {
        Optional<TopicEntity> existing = topicRepository.findById(topicId);
        if (existing.isPresent()) {
            TopicEntity entity = existing.get();
            entity.setName(topic.getName());
            entity.setDescription(topic.getDescription());
            topicRepository.save(entity);
            System.out.println("✅ Updated topic: " + topicId);
        }
    }

    @Override
    @Transactional
    public void deleteTopic(String topicId) {
        questionRepository.deleteByTopicId(topicId);
        topicRepository.deleteById(topicId);
        System.out.println("🗑️ Deleted topic: " + topicId);
    }

    @Override
    @Transactional
    public void addQuestionToTopic(String topicId, Question question) {
        if (!topicRepository.existsById(topicId)) {
            System.err.println("⚠️ Cannot add question - topic not found: " + topicId);
            return;
        }

        if (question.getId() == null || question.getId().isEmpty()) {
            question.setId(UUID.randomUUID().toString());
        }

        QuestionEntity entity = new QuestionEntity(
                question.getId(),
                question.getText(),
                question.getOptions(),
                question.getCorrectIndex(),
                topicId
        );

        questionRepository.save(entity);
        System.out.println("✅ Saved question to database for topic: " + topicId);
    }

    @Override
    @Transactional
    public void updateQuestion(String questionId, Question question) {
        Optional<QuestionEntity> existing = questionRepository.findById(questionId);
        if (existing.isPresent()) {
            QuestionEntity entity = existing.get();
            entity.setText(question.getText());
            entity.setOptions(question.getOptions());
            entity.setCorrectIndex(question.getCorrectIndex());
            questionRepository.save(entity);
            System.out.println("✅ Updated question: " + questionId);
        }
    }

    @Override
    @Transactional
    public void deleteQuestion(String questionId) {
        questionRepository.deleteById(questionId);
        System.out.println("🗑️ Deleted question: " + questionId);
    }

    @Override
    public List<Question> getQuestionsByTopicId(String topicId) {
        return questionRepository.findByTopicId(topicId).stream()
                .map(qe -> new Question(qe.getId(), qe.getText(), qe.getOptions(), qe.getCorrectIndex()))
                .collect(Collectors.toList());
    }
}