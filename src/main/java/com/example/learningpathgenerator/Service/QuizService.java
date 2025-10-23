package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.model.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class QuizService {

    private final Map<String, Quiz> quizzes = new HashMap<>();

    @PostConstruct
    public void init() {
        // Seed a couple of simple quizzes; extend as needed
        Quiz javaBasics = new Quiz("t-java-basics", "Java Basics", Arrays.asList(
                new Question("q1", "What is the entry point method name in a Java application?", Arrays.asList("start()", "main()", "run()", "init()"), 1),
                new Question("q2", "Which keyword is used to create a new object?", Arrays.asList("construct", "new", "create", "instantiate"), 1),
                new Question("q3", "Which is a primitive type?", Arrays.asList("String", "Integer", "int", "List"), 2)
        ));

        Quiz algorithms = new Quiz("t-data-structures", "Algorithms & DS", Arrays.asList(
                new Question("q1", "What is the time complexity of binary search on sorted array?", Arrays.asList("O(n)", "O(log n)", "O(n log n)", "O(1)"), 1),
                new Question("q2", "Which data structure is FIFO?", Arrays.asList("Stack", "Queue", "Tree", "Graph"), 1),
                new Question("q3", "Which algorithm is used for shortest paths in weighted graphs?", Arrays.asList("Kruskal", "Dijkstra", "Prim", "MergeSort"), 1)
        ));

        quizzes.put(javaBasics.getTopicId(), javaBasics);
        quizzes.put(algorithms.getTopicId(), algorithms);
    }

    public Optional<Quiz> findByTopicId(String topicId) {
        return Optional.ofNullable(quizzes.get(topicId));
    }

    public Set<String> availableTopicIds() {
        return quizzes.keySet();
    }
}