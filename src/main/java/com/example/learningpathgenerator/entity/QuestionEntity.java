package com.example.learningpathgenerator.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;

    @Column(name = "text", length = 1000)
    private String text;

    @ElementCollection
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option")
    private List<String> options;

    @Column(name = "correct_answer_index")
    private Integer correctAnswerIndex;

    @Column(name = "correct_index")
    private Integer correctIndex;

    private String explanation;
    private String topic;
    private String difficulty;

    public QuestionEntity() {}

    public QuestionEntity(String questionText, List<String> options, Integer correctAnswerIndex) {
        this.questionText = questionText;
        this.text = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.correctIndex = correctAnswerIndex;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
        this.text = questionText; // Keep in sync
    }

    public String getText() { return text != null ? text : questionText; }
    public void setText(String text) {
        this.text = text;
        this.questionText = text; // Keep in sync
    }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public Integer getCorrectAnswerIndex() { return correctAnswerIndex; }
    public void setCorrectAnswerIndex(Integer correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
        this.correctIndex = correctAnswerIndex; // Keep in sync
    }

    public Integer getCorrectIndex() { return correctIndex != null ? correctIndex : correctAnswerIndex; }
    public void setCorrectIndex(Integer correctIndex) {
        this.correctIndex = correctIndex;
        this.correctAnswerIndex = correctIndex; // Keep in sync
    }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}