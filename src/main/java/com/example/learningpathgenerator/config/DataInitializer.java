package com.example.learningpathgenerator.config;

import com.example.learningpathgenerator.entity.TopicEntity;
import com.example.learningpathgenerator.entity.QuestionEntity;
import com.example.learningpathgenerator.model.User;
import com.example.learningpathgenerator.repository.TopicRepository;
import com.example.learningpathgenerator.repository.QuestionRepository;
import com.example.learningpathgenerator.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           TopicRepository topicRepository,
                           QuestionRepository questionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("🚀 STARTING DATA INITIALIZATION");
        System.out.println("========================================\n");

        // Create admin user
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of("ROLE_ADMIN"));
            userRepository.save(admin);
            System.out.println("✅ Admin user created: admin/admin123");
        } else {
            System.out.println("👤 Admin user already exists");
        }

        // Create regular user
        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRoles(Set.of("ROLE_USER"));
            userRepository.save(user);
            System.out.println("✅ Regular user created: user/user123");
        } else {
            System.out.println("👤 Regular user already exists");
        }

        // Initialize default topics if database is empty
        long topicCount = topicRepository.count();
        System.out.println("\n📊 Current topic count in database: " + topicCount);

        if (topicCount == 0) {
            System.out.println("🔧 Initializing default topics and questions...\n");

            // Java Basics
            TopicEntity javaTopic = new TopicEntity("java-basics", "Java Programming Basics",
                    "Learn fundamental Java programming concepts");
            topicRepository.save(javaTopic);
            System.out.println("✅ Saved: " + javaTopic.getId() + " - " + javaTopic.getName());

            // Verify
            TopicEntity verifyJava = topicRepository.findById("java-basics").orElse(null);
            if (verifyJava != null) {
                System.out.println("   ✓ Verified in DB: " + verifyJava.getName());
            } else {
                System.out.println("   ✗ ERROR: Topic not found after save!");
            }

            questionRepository.save(new QuestionEntity("q1",
                    "What is the correct way to declare a variable in Java?",
                    Arrays.asList("var x = 5;", "int x = 5;", "x := 5;", "declare x as int = 5;"),
                    1, "java-basics"));

            questionRepository.save(new QuestionEntity("q2",
                    "Which keyword is used to create a class in Java?",
                    Arrays.asList("class", "Class", "define", "struct"),
                    0, "java-basics"));

            questionRepository.save(new QuestionEntity("q3",
                    "What is the entry point of a Java application?",
                    Arrays.asList("start()", "main()", "run()", "execute()"),
                    1, "java-basics"));

            System.out.println("   ✓ Added 3 questions\n");

            // Python Basics
            TopicEntity pythonTopic = new TopicEntity("python-basics", "Python Programming Basics",
                    "Learn fundamental Python programming concepts");
            topicRepository.save(pythonTopic);
            System.out.println("✅ Saved: " + pythonTopic.getId() + " - " + pythonTopic.getName());

            questionRepository.save(new QuestionEntity("q4",
                    "How do you create a list in Python?",
                    Arrays.asList("list = []", "list = ()", "list = {}", "list = <>"),
                    0, "python-basics"));

            questionRepository.save(new QuestionEntity("q5",
                    "Which keyword is used to define a function in Python?",
                    Arrays.asList("function", "def", "func", "define"),
                    1, "python-basics"));

            questionRepository.save(new QuestionEntity("q6",
                    "What is the correct file extension for Python files?",
                    Arrays.asList(".pt", ".pyt", ".py", ".python"),
                    2, "python-basics"));

            System.out.println("   ✓ Added 3 questions\n");

            // Web Development
            TopicEntity webTopic = new TopicEntity("web-development", "Web Development Fundamentals",
                    "Learn HTML, CSS, and JavaScript basics");
            topicRepository.save(webTopic);
            System.out.println("✅ Saved: " + webTopic.getId() + " - " + webTopic.getName());

            questionRepository.save(new QuestionEntity("q7",
                    "What does HTML stand for?",
                    Arrays.asList("Hyper Text Markup Language", "High Tech Modern Language",
                            "Home Tool Markup Language", "Hyperlinks and Text Markup Language"),
                    0, "web-development"));

            questionRepository.save(new QuestionEntity("q8",
                    "Which HTML tag is used to create a hyperlink?",
                    Arrays.asList("<link>", "<a>", "<href>", "<url>"),
                    1, "web-development"));

            questionRepository.save(new QuestionEntity("q9",
                    "What does CSS stand for?",
                    Arrays.asList("Computer Style Sheets", "Creative Style Sheets",
                            "Cascading Style Sheets", "Colorful Style Sheets"),
                    2, "web-development"));

            System.out.println("   ✓ Added 3 questions\n");

            // Database Basics
            TopicEntity dbTopic = new TopicEntity("database-basics", "Database Fundamentals",
                    "Learn SQL and database design basics");
            topicRepository.save(dbTopic);
            System.out.println("✅ Saved: " + dbTopic.getId() + " - " + dbTopic.getName());

            questionRepository.save(new QuestionEntity("q10",
                    "Which SQL statement is used to retrieve data from a database?",
                    Arrays.asList("GET", "RETRIEVE", "SELECT", "FETCH"),
                    2, "database-basics"));

            questionRepository.save(new QuestionEntity("q11",
                    "What does SQL stand for?",
                    Arrays.asList("Structured Query Language", "Simple Query Language",
                            "Standard Question Language", "System Query Logic"),
                    0, "database-basics"));

            questionRepository.save(new QuestionEntity("q12",
                    "Which command is used to create a new table in SQL?",
                    Arrays.asList("NEW TABLE", "CREATE TABLE", "MAKE TABLE", "ADD TABLE"),
                    1, "database-basics"));

            System.out.println("   ✓ Added 3 questions\n");

            // Data Structures
            TopicEntity dsTopic = new TopicEntity("data-structures", "Data Structures & Algorithms",
                    "Learn fundamental data structures and algorithms");
            topicRepository.save(dsTopic);
            System.out.println("✅ Saved: " + dsTopic.getId() + " - " + dsTopic.getName());

            questionRepository.save(new QuestionEntity("q13",
                    "What is the time complexity of accessing an element in an array by index?",
                    Arrays.asList("O(n)", "O(1)", "O(log n)", "O(n²)"),
                    1, "data-structures"));

            questionRepository.save(new QuestionEntity("q14",
                    "Which data structure uses LIFO (Last In First Out) principle?",
                    Arrays.asList("Queue", "Array", "Stack", "Tree"),
                    2, "data-structures"));

            questionRepository.save(new QuestionEntity("q15",
                    "What is a linked list?",
                    Arrays.asList("An array with fixed size", "A collection of nodes with pointers",
                            "A type of tree", "A sorting algorithm"),
                    1, "data-structures"));

            System.out.println("   ✓ Added 3 questions\n");

            System.out.println("========================================");
            System.out.println("✅ INITIALIZATION COMPLETE");
            System.out.println("📊 Total topics: " + topicRepository.count());
            System.out.println("📊 Total questions: " + questionRepository.count());
            System.out.println("========================================\n");

            // Print all topics for verification
            System.out.println("📋 ALL TOPICS IN DATABASE:");
            topicRepository.findAll().forEach(topic -> {
                System.out.println("   🔹 ID: " + topic.getId());
                System.out.println("      Name: " + topic.getName());
                System.out.println("      Description: " +
                        (topic.getDescription() != null ?
                                topic.getDescription().substring(0, Math.min(50, topic.getDescription().length())) + "..."
                                : "null"));
                System.out.println();
            });

        } else {
            System.out.println("📚 Database already has " + topicCount + " topics");

            // Print existing topics
            System.out.println("\n📋 EXISTING TOPICS:");
            topicRepository.findAll().forEach(topic -> {
                System.out.println("   🔹 " + topic.getId() + " - " + topic.getName());
            });
            System.out.println();
        }

        System.out.println("========================================");
        System.out.println("✅ DATA INITIALIZER FINISHED");
        System.out.println("========================================\n");
    }
}