package com.example.learningpathgenerator.config;

import com.example.learningpathgenerator.entity.*;
import com.example.learningpathgenerator.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            QuizRepository quizRepository,
            LearningResourceRepository resourceRepository) {

        return args -> {
            log.info("Initializing sample data...");

            // Create sample user
            if (userRepository.count() == 0) {
                User user = new User();
                user.setUsername("demo_user");
                user.setEmail("demo@example.com");
                user.setPassword("password123");
                user.setCreatedAt(LocalDateTime.now());
                user.setLastActiveAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("Created demo user: demo_user");
            }

            // Create sample quizzes
            if (quizRepository.count() == 0) {
                createJavaQuiz(quizRepository);
                createPythonQuiz(quizRepository);
                log.info("Created sample quizzes");
            }

            // Create sample resources
            if (resourceRepository.count() == 0) {
                createSampleResources(resourceRepository);
                log.info("Created sample resources");
            }

            log.info("Data initialization complete!");
            log.info("Users: {}, Quizzes: {}, Resources: {}",
                    userRepository.count(),
                    quizRepository.count(),
                    resourceRepository.count());
        };
    }

    private void createJavaQuiz(QuizRepository quizRepository) {
        Quiz quiz = new Quiz();
        quiz.setTitle("Java Fundamentals Quiz");
        quiz.setDescription("Test your knowledge of Java basics");
        quiz.setTopic("Java Programming");
        quiz.setDifficultyLevel("BEGINNER");
        quiz.setTimeLimit(30);
        quiz.setPassingScore(3);

        List<Question> questions = new ArrayList<>();

        // Question 1
        Question q1 = new Question();
        q1.setQuiz(quiz);
        q1.setQuestionText("What is the default value of a boolean variable in Java?");
        q1.setQuestionType("MULTIPLE_CHOICE");
        q1.setOptions(List.of("true", "false", "null", "0"));
        q1.setCorrectAnswer("false");
        q1.setPoints(1);
        q1.setSkillsTested(List.of("Java Basics", "Data Types"));
        q1.setDifficultyLevel("BEGINNER");
        questions.add(q1);

        // Question 2
        Question q2 = new Question();
        q2.setQuiz(quiz);
        q2.setQuestionText("Which keyword is used to inherit a class in Java?");
        q2.setQuestionType("MULTIPLE_CHOICE");
        q2.setOptions(List.of("extends", "implements", "inherits", "super"));
        q2.setCorrectAnswer("extends");
        q2.setPoints(1);
        q2.setSkillsTested(List.of("OOP", "Inheritance"));
        q2.setDifficultyLevel("BEGINNER");
        questions.add(q2);

        // Question 3
        Question q3 = new Question();
        q3.setQuiz(quiz);
        q3.setQuestionText("What is the size of int in Java?");
        q3.setQuestionType("MULTIPLE_CHOICE");
        q3.setOptions(List.of("8 bits", "16 bits", "32 bits", "64 bits"));
        q3.setCorrectAnswer("32 bits");
        q3.setPoints(1);
        q3.setSkillsTested(List.of("Java Basics", "Data Types"));
        q3.setDifficultyLevel("BEGINNER");
        questions.add(q3);

        // Question 4
        Question q4 = new Question();
        q4.setQuiz(quiz);
        q4.setQuestionText("Which method is the entry point of a Java application?");
        q4.setQuestionType("MULTIPLE_CHOICE");
        q4.setOptions(List.of("start()", "main()", "run()", "execute()"));
        q4.setCorrectAnswer("main()");
        q4.setPoints(1);
        q4.setSkillsTested(List.of("Java Basics", "Program Structure"));
        q4.setDifficultyLevel("BEGINNER");
        questions.add(q4);

        // Question 5
        Question q5 = new Question();
        q5.setQuiz(quiz);
        q5.setQuestionText("What is encapsulation in Java?");
        q5.setQuestionType("MULTIPLE_CHOICE");
        q5.setOptions(List.of(
                "Hiding implementation details",
                "Using multiple classes",
                "Creating objects",
                "Defining methods"
        ));
        q5.setCorrectAnswer("Hiding implementation details");
        q5.setPoints(1);
        q5.setSkillsTested(List.of("OOP", "Encapsulation"));
        q5.setDifficultyLevel("INTERMEDIATE");
        questions.add(q5);

        quiz.setQuestions(questions);
        quizRepository.save(quiz);
    }

    private void createPythonQuiz(QuizRepository quizRepository) {
        Quiz quiz = new Quiz();
        quiz.setTitle("Python Basics Quiz");
        quiz.setDescription("Test your Python fundamentals");
        quiz.setTopic("Python Programming");
        quiz.setDifficultyLevel("BEGINNER");
        quiz.setTimeLimit(25);
        quiz.setPassingScore(3);

        List<Question> questions = new ArrayList<>();

        // Question 1
        Question q1 = new Question();
        q1.setQuiz(quiz);
        q1.setQuestionText("Which of the following is used to define a function in Python?");
        q1.setQuestionType("MULTIPLE_CHOICE");
        q1.setOptions(List.of("function", "def", "func", "define"));
        q1.setCorrectAnswer("def");
        q1.setPoints(1);
        q1.setSkillsTested(List.of("Python Basics", "Functions"));
        q1.setDifficultyLevel("BEGINNER");
        questions.add(q1);

        // Question 2
        Question q2 = new Question();
        q2.setQuiz(quiz);
        q2.setQuestionText("What is the output of: print(type([]))");
        q2.setQuestionType("MULTIPLE_CHOICE");
        q2.setOptions(List.of("<class 'list'>", "<class 'dict'>", "<class 'tuple'>", "<class 'set'>"));
        q2.setCorrectAnswer("<class 'list'>");
        q2.setPoints(1);
        q2.setSkillsTested(List.of("Python Basics", "Data Structures"));
        q2.setDifficultyLevel("BEGINNER");
        questions.add(q2);

        // Question 3
        Question q3 = new Question();
        q3.setQuiz(quiz);
        q3.setQuestionText("Which data structure is immutable in Python?");
        q3.setQuestionType("MULTIPLE_CHOICE");
        q3.setOptions(List.of("List", "Dictionary", "Tuple", "Set"));
        q3.setCorrectAnswer("Tuple");
        q3.setPoints(1);
        q3.setSkillsTested(List.of("Python Basics", "Data Structures"));
        q3.setDifficultyLevel("BEGINNER");
        questions.add(q3);

        // Question 4
        Question q4 = new Question();
        q4.setQuiz(quiz);
        q4.setQuestionText("What does 'self' represent in Python?");
        q4.setQuestionType("MULTIPLE_CHOICE");
        q4.setOptions(List.of("The class itself", "The instance of the class", "A global variable", "A keyword"));
        q4.setCorrectAnswer("The instance of the class");
        q4.setPoints(1);
        q4.setSkillsTested(List.of("OOP", "Python Basics"));
        q4.setDifficultyLevel("INTERMEDIATE");
        questions.add(q4);

        quiz.setQuestions(questions);
        quizRepository.save(quiz);
    }

    private void createSampleResources(LearningResourceRepository resourceRepository) {
        // Java Resources
        LearningResource r1 = new LearningResource();
        r1.setTitle("Java Programming Tutorial for Beginners");
        r1.setDescription("Complete Java tutorial covering all fundamentals");
        r1.setResourceType("VIDEO");
        r1.setUrl("https://www.youtube.com/watch?v=eIrMbAQSU34");
        r1.setProvider("YouTube");
        r1.setEstimatedDuration(180);
        r1.setTags(List.of("Java Basics", "OOP", "Programming"));
        r1.setDifficultyLevel("BEGINNER");
        resourceRepository.save(r1);

        LearningResource r2 = new LearningResource();
        r2.setTitle("Java OOP Concepts");
        r2.setDescription("Deep dive into Object-Oriented Programming in Java");
        r2.setResourceType("ARTICLE");
        r2.setUrl("https://www.geeksforgeeks.org/object-oriented-programming-oops-concept-in-java/");
        r2.setProvider("GeeksforGeeks");
        r2.setEstimatedDuration(45);
        r2.setTags(List.of("OOP", "Inheritance", "Encapsulation", "Polymorphism"));
        r2.setDifficultyLevel("INTERMEDIATE");
        resourceRepository.save(r2);

        LearningResource r3 = new LearningResource();
        r3.setTitle("Java Practice Exercises");
        r3.setDescription("Hands-on coding exercises for Java");
        r3.setResourceType("EXERCISE");
        r3.setUrl("https://www.w3resource.com/java-exercises/");
        r3.setProvider("W3Resource");
        r3.setEstimatedDuration(120);
        r3.setTags(List.of("Java Basics", "Practice", "Coding"));
        r3.setDifficultyLevel("BEGINNER");
        resourceRepository.save(r3);

        // Python Resources
        LearningResource r4 = new LearningResource();
        r4.setTitle("Python for Beginners");
        r4.setDescription("Learn Python from scratch");
        r4.setResourceType("VIDEO");
        r4.setUrl("https://www.youtube.com/watch?v=rfscVS0vtbw");
        r4.setProvider("YouTube");
        r4.setEstimatedDuration(240);
        r4.setTags(List.of("Python Basics", "Functions", "Data Structures"));
        r4.setDifficultyLevel("BEGINNER");
        resourceRepository.save(r4);

        LearningResource r5 = new LearningResource();
        r5.setTitle("Python Data Structures");
        r5.setDescription("Master lists, dictionaries, sets, and tuples");
        r5.setResourceType("TUTORIAL");
        r5.setUrl("https://docs.python.org/3/tutorial/datastructures.html");
        r5.setProvider("Python.org");
        r5.setEstimatedDuration(60);
        r5.setTags(List.of("Data Structures", "Python Basics"));
        r5.setDifficultyLevel("BEGINNER");
        resourceRepository.save(r5);

        LearningResource r6 = new LearningResource();
        r6.setTitle("Build a Python Project");
        r6.setDescription("Create a real-world Python application");
        r6.setResourceType("PROJECT");
        r6.setUrl("https://realpython.com/tutorials/projects/");
        r6.setProvider("Real Python");
        r6.setEstimatedDuration(300);
        r6.setTags(List.of("Python Basics", "Project", "Practical"));
        r6.setDifficultyLevel("INTERMEDIATE");
        resourceRepository.save(r6);
    }
}