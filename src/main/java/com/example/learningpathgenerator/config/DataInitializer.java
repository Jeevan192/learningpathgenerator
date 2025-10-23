package com.example.learningpathgenerator.config;

import com.example.learningpathgenerator.model.Topic;
import com.example.learningpathgenerator.repository.TopicRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TopicRepository topicRepository;

    public DataInitializer(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (topicRepository.count() > 0) return;

        Topic t1 = new Topic(
                "t-java-basics",
                "Java Syntax & Basics",
                "core",
                1,
                8,
                Arrays.asList("https://docs.oracle.com/javase/tutorial/java/nutsandbolts/index.html", "https://www.w3schools.com/java/"),
                List.of(),
                new HashSet<>(Arrays.asList("java", "core", "syntax"))
        );

        Topic t2 = new Topic(
                "t-oop",
                "Object-Oriented Programming in Java",
                "core",
                2,
                12,
                Arrays.asList("https://www.baeldung.com/java-oop", "https://www.oracle.com/technical-resources/articles/java/javadevelopers.html"),
                List.of("t-java-basics"),
                new HashSet<>(Arrays.asList("java", "oop", "core"))
        );

        Topic t3 = new Topic(
                "t-collections",
                "Collections Framework",
                "core",
                3,
                10,
                Arrays.asList("https://docs.oracle.com/javase/8/docs/technotes/guides/collections/overview.html"),
                List.of("t-oop"),
                new HashSet<>(Arrays.asList("collections", "data-structures", "core"))
        );

        Topic t4 = new Topic(
                "t-concurrency",
                "Concurrency & Multithreading",
                "advanced",
                5,
                18,
                Arrays.asList("https://docs.oracle.com/javase/tutorial/essential/concurrency/"),
                List.of("t-java-basics", "t-oop"),
                new HashSet<>(Arrays.asList("concurrency", "advanced"))
        );

        Topic t5 = new Topic(
                "t-streams",
                "Streams & Functional Programming",
                "core",
                3,
                8,
                Arrays.asList("https://www.baeldung.com/java-8-streams"),
                List.of("t-java-basics"),
                new HashSet<>(Arrays.asList("streams", "functional", "core"))
        );

        Topic t6 = new Topic(
                "t-data-structures",
                "Algorithms & Data Structures",
                "algorithms",
                4,
                20,
                Arrays.asList("https://www.geeksforgeeks.org/data-structures/", "https://visualgo.net/en"),
                List.of("t-collections"),
                new HashSet<>(Arrays.asList("algorithms", "data-structures"))
        );

        Topic t7 = new Topic(
                "t-spring",
                "Spring Basics",
                "web",
                4,
                20,
                Arrays.asList("https://spring.io/guides/gs/spring-boot/"),
                List.of("t-java-basics", "t-oop"),
                new HashSet<>(Arrays.asList("spring", "web", "framework"))
        );

        Topic t8 = new Topic(
                "t-rest",
                "Building REST APIs with Spring",
                "web",
                4,
                12,
                Arrays.asList("https://spring.io/guides/gs/rest-service/"),
                List.of("t-spring"),
                new HashSet<>(Arrays.asList("rest", "web", "api"))
        );

        Topic t9 = new Topic(
                "t-testing",
                "Unit Testing with JUnit",
                "testing",
                2,
                6,
                Arrays.asList("https://junit.org/junit5/"),
                List.of("t-java-basics"),
                new HashSet<>(Arrays.asList("testing", "junit"))
        );

        Topic t10 = new Topic(
                "t-build-tools",
                "Maven / Gradle Basics",
                "tools",
                2,
                6,
                Arrays.asList("https://maven.apache.org/guides/"),
                List.of("t-java-basics"),
                new HashSet<>(Arrays.asList("maven", "gradle", "build-tools"))
        );

        topicRepository.saveAll(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10));
    }
}