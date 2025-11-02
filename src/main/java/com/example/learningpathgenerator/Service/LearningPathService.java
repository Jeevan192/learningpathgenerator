package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.dto.GenerateRequest;
import com.example.learningpathgenerator.model.LearningPath;
import com.example.learningpathgenerator.model.module;
import com.example.learningpathgenerator.model.Topic;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.*;

import java.util.*;

@Service
public class LearningPathService {

    private final QuizService quizService;

    public LearningPathService(QuizService quizService) {
        this.quizService = quizService;
    }

    public LearningPath generatePath(GenerateRequest request) {
        List<Topic> allTopics = quizService.getAllTopics();

        String skill = Optional.ofNullable(request.getSkillLevel()).orElse("BEGINNER").toUpperCase(Locale.ROOT);
        Set<String> interests = Optional.ofNullable(request.getInterests())
                .map(list -> list.stream().map(String::toLowerCase).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        // Generate detailed modules based on quiz topics and skill level
        List<module> modules = new ArrayList<>();

        for (String interest : interests) {
            // Find matching topic
            Optional<Topic> matchingTopic = allTopics.stream()
                    .filter(t -> t.getName().toLowerCase().contains(interest) ||
                            t.getId().toLowerCase().contains(interest))
                    .findFirst();

            if (matchingTopic.isPresent()) {
                Topic topic = matchingTopic.get();
                modules.addAll(generateModulesForTopic(topic, skill));
            }
        }

        // If no modules generated, create default comprehensive path
        if (modules.isEmpty()) {
            modules = generateDefaultModules(skill, interests);
        }

        int totalHours = modules.stream().mapToInt(module::getHours).sum();
        int weekly = Math.max(1, request.getWeeklyHours());
        int estimatedWeeks = (int) Math.ceil((double) totalHours / weekly);

        String title = generateTitle(request.getTarget(), request.getName(), skill);

        return new LearningPath(
                title,
                Optional.ofNullable(request.getName()).orElse("Learner"),
                skill,
                weekly,
                estimatedWeeks,
                totalHours,
                modules
        );
    }

    private List<module> generateModulesForTopic(Topic topic, String skillLevel) {
        List<module> modules = new ArrayList<>();
        String topicName = topic.getName();
        String topicId = topic.getId();

        // Determine base hours based on skill level
        int baseHours = getBaseHours(skillLevel);

        // Generate subtopics based on topic type
        if (topicId.contains("java")) {
            modules.addAll(generateJavaModules(baseHours, skillLevel));
        } else if (topicId.contains("python")) {
            modules.addAll(generatePythonModules(baseHours, skillLevel));
        } else if (topicId.contains("web")) {
            modules.addAll(generateWebModules(baseHours, skillLevel));
        } else if (topicId.contains("database")) {
            modules.addAll(generateDatabaseModules(baseHours, skillLevel));
        } else if (topicId.contains("data-structure")) {
            modules.addAll(generateDataStructuresModules(baseHours, skillLevel));
        } else {
            // Generic modules for custom topics
            modules.addAll(generateGenericModules(topicName, baseHours, skillLevel));
        }

        return modules;
    }

    private List<module> generateJavaModules(int baseHours, String skillLevel) {
        List<module> modules = new ArrayList<>();

        modules.add(new module(
                "Java Fundamentals",
                "Learn Java syntax, variables, data types, operators, and control structures",
                baseHours,
                Arrays.asList("Variables and Data Types", "Operators", "Control Flow", "Loops")
        ));

        modules.add(new module(
                "Object-Oriented Programming in Java",
                "Master classes, objects, inheritance, polymorphism, and encapsulation",
                baseHours + 2,
                Arrays.asList("Classes and Objects", "Inheritance", "Polymorphism", "Abstraction", "Encapsulation")
        ));

        modules.add(new module(
                "Java Collections Framework",
                "Understand Lists, Sets, Maps, and advanced collection operations",
                baseHours + 1,
                Arrays.asList("ArrayList and LinkedList", "HashSet and TreeSet", "HashMap and TreeMap", "Iterators")
        ));

        if (skillLevel.equals("INTERMEDIATE") || skillLevel.equals("ADVANCED")) {
            modules.add(new module(
                    "Exception Handling & File I/O",
                    "Learn to handle errors gracefully and work with files",
                    baseHours,
                    Arrays.asList("Try-Catch Blocks", "Custom Exceptions", "File Reading", "File Writing")
            ));

            modules.add(new module(
                    "Java Streams and Lambda Expressions",
                    "Master functional programming concepts in Java",
                    baseHours + 2,
                    Arrays.asList("Lambda Syntax", "Stream Operations", "Collectors", "Optional")
            ));
        }

        if (skillLevel.equals("ADVANCED")) {
            modules.add(new module(
                    "Multithreading and Concurrency",
                    "Build concurrent applications with threads and executors",
                    baseHours + 3,
                    Arrays.asList("Thread Basics", "Synchronization", "Executor Framework", "Concurrent Collections")
            ));
        }

        return modules;
    }

    private List<module> generatePythonModules(int baseHours, String skillLevel) {
        List<module> modules = new ArrayList<>();

        modules.add(new module(
                "Python Basics",
                "Learn Python syntax, variables, data types, and basic operations",
                baseHours,
                Arrays.asList("Variables and Types", "Strings", "Numbers", "Boolean Logic", "Input/Output")
        ));

        modules.add(new module(
                "Python Data Structures",
                "Master lists, tuples, dictionaries, and sets",
                baseHours + 1,
                Arrays.asList("Lists", "Tuples", "Dictionaries", "Sets", "List Comprehensions")
        ));

        modules.add(new module(
                "Functions and Modules",
                "Create reusable code with functions and organize with modules",
                baseHours,
                Arrays.asList("Function Definition", "Parameters", "Return Values", "Lambda Functions", "Modules")
        ));

        if (skillLevel.equals("INTERMEDIATE") || skillLevel.equals("ADVANCED")) {
            modules.add(new module(
                    "Object-Oriented Python",
                    "Build classes and use OOP principles in Python",
                    baseHours + 2,
                    Arrays.asList("Classes", "Inheritance", "Magic Methods", "Property Decorators")
            ));

            modules.add(new module(
                    "File Handling and Error Management",
                    "Work with files and handle exceptions properly",
                    baseHours,
                    Arrays.asList("Reading Files", "Writing Files", "Context Managers", "Exception Handling")
            ));
        }

        if (skillLevel.equals("ADVANCED")) {
            modules.add(new module(
                    "Advanced Python Concepts",
                    "Decorators, generators, and advanced programming techniques",
                    baseHours + 3,
                    Arrays.asList("Decorators", "Generators", "Context Managers", "Metaclasses")
            ));
        }

        return modules;
    }

    private List<module> generateWebModules(int baseHours, String skillLevel) {
        List<module> modules = new ArrayList<>();

        modules.add(new module(
                "HTML Fundamentals",
                "Build web page structure with HTML5",
                baseHours,
                Arrays.asList("HTML Tags", "Forms", "Semantic HTML", "HTML5 Features")
        ));

        modules.add(new module(
                "CSS Styling",
                "Style web pages with CSS and responsive design",
                baseHours + 1,
                Arrays.asList("CSS Selectors", "Box Model", "Flexbox", "Grid", "Responsive Design")
        ));

        modules.add(new module(
                "JavaScript Basics",
                "Add interactivity with JavaScript fundamentals",
                baseHours + 2,
                Arrays.asList("Variables", "Functions", "DOM Manipulation", "Events", "ES6 Features")
        ));

        if (skillLevel.equals("INTERMEDIATE") || skillLevel.equals("ADVANCED")) {
            modules.add(new module(
                    "Modern JavaScript",
                    "Master async programming and modern JS features",
                    baseHours + 2,
                    Arrays.asList("Promises", "Async/Await", "Fetch API", "Modules", "Classes")
            ));

            modules.add(new module(
                    "Frontend Framework Basics",
                    "Introduction to React or Vue.js",
                    baseHours + 3,
                    Arrays.asList("Components", "State Management", "Props", "Lifecycle", "Hooks")
            ));
        }

        if (skillLevel.equals("ADVANCED")) {
            modules.add(new module(
                    "Full-Stack Development",
                    "Build complete web applications with backend integration",
                    baseHours + 4,
                    Arrays.asList("REST APIs", "Authentication", "Database Integration", "Deployment")
            ));
        }

        return modules;
    }

    private List<module> generateDatabaseModules(int baseHours, String skillLevel) {
        List<module> modules = new ArrayList<>();

        modules.add(new module(
                "Database Fundamentals",
                "Understand relational databases and SQL basics",
                baseHours,
                Arrays.asList("Tables", "Data Types", "Primary Keys", "Foreign Keys", "Relationships")
        ));

        modules.add(new module(
                "SQL Queries",
                "Master SELECT, INSERT, UPDATE, and DELETE operations",
                baseHours + 1,
                Arrays.asList("SELECT Queries", "WHERE Clauses", "Joins", "Aggregations", "Subqueries")
        ));

        modules.add(new module(
                "Database Design",
                "Learn normalization and efficient database design",
                baseHours + 2,
                Arrays.asList("Normalization", "ER Diagrams", "Schema Design", "Indexes")
        ));

        if (skillLevel.equals("INTERMEDIATE") || skillLevel.equals("ADVANCED")) {
            modules.add(new module(
                    "Advanced SQL",
                    "Complex queries, views, and stored procedures",
                    baseHours + 2,
                    Arrays.asList("Views", "Stored Procedures", "Triggers", "Transactions", "CTEs")
            ));
        }

        if (skillLevel.equals("ADVANCED")) {
            modules.add(new module(
                    "Database Performance & Optimization",
                    "Optimize queries and database performance",
                    baseHours + 3,
                    Arrays.asList("Query Optimization", "Indexing Strategies", "Execution Plans", "Caching")
            ));
        }

        return modules;
    }

    private List<module> generateDataStructuresModules(int baseHours, String skillLevel) {
        List<module> modules = new ArrayList<>();

        modules.add(new module(
                "Arrays and Strings",
                "Master fundamental data structures",
                baseHours,
                Arrays.asList("Array Operations", "String Manipulation", "Two Pointers", "Sliding Window")
        ));

        modules.add(new module(
                "Linked Lists",
                "Understand and implement linked list structures",
                baseHours + 1,
                Arrays.asList("Singly Linked Lists", "Doubly Linked Lists", "Circular Lists", "Operations")
        ));

        modules.add(new module(
                "Stacks and Queues",
                "Learn LIFO and FIFO data structures",
                baseHours + 1,
                Arrays.asList("Stack Operations", "Queue Operations", "Deque", "Priority Queue")
        ));

        modules.add(new module(
                "Trees and Graphs",
                "Work with hierarchical and graph data structures",
                baseHours + 2,
                Arrays.asList("Binary Trees", "BST", "Tree Traversals", "Graph Representations", "BFS/DFS")
        ));

        if (skillLevel.equals("INTERMEDIATE") || skillLevel.equals("ADVANCED")) {
            modules.add(new module(
                    "Hash Tables and Sets",
                    "Efficient lookup with hashing techniques",
                    baseHours + 1,
                    Arrays.asList("Hash Functions", "Collision Resolution", "HashMap Implementation", "HashSet")
            ));

            modules.add(new module(
                    "Sorting and Searching Algorithms",
                    "Master common algorithms",
                    baseHours + 2,
                    Arrays.asList("Quick Sort", "Merge Sort", "Binary Search", "Heap Sort")
            ));
        }

        if (skillLevel.equals("ADVANCED")) {
            modules.add(new module(
                    "Advanced Data Structures",
                    "Heaps, tries, and advanced structures",
                    baseHours + 3,
                    Arrays.asList("Heaps", "Tries", "Segment Trees", "Disjoint Sets", "AVL Trees")
            ));
        }

        return modules;
    }

    private List<module> generateGenericModules(String topicName, int baseHours, String skillLevel) {
        List<module> modules = new ArrayList<>();

        modules.add(new module(
                topicName + " - Introduction",
                "Get started with " + topicName + " fundamentals",
                baseHours,
                Arrays.asList("Overview", "Core Concepts", "Basic Terminology", "Getting Started")
        ));

        modules.add(new module(
                topicName + " - Intermediate Concepts",
                "Build on your " + topicName + " knowledge",
                baseHours + 2,
                Arrays.asList("Advanced Features", "Best Practices", "Common Patterns", "Practical Examples")
        ));

        modules.add(new module(
                topicName + " - Practical Application",
                "Apply " + topicName + " in real projects",
                baseHours + 2,
                Arrays.asList("Project Setup", "Implementation", "Testing", "Deployment")
        ));

        if (skillLevel.equals("ADVANCED")) {
            modules.add(new module(
                    topicName + " - Advanced Topics",
                    "Master advanced " + topicName + " techniques",
                    baseHours + 3,
                    Arrays.asList("Optimization", "Architecture", "Performance", "Industry Practices")
            ));
        }

        return modules;
    }

    private List<module> generateDefaultModules(String skillLevel, Set<String> interests) {
        List<module> modules = new ArrayList<>();
        int baseHours = getBaseHours(skillLevel);

        String interestStr = interests.isEmpty() ? "Programming" : String.join(", ", interests);

        modules.add(new module(
                "Foundations",
                "Build a strong foundation in " + interestStr,
                baseHours,
                Arrays.asList("Core Concepts", "Terminology", "Environment Setup", "First Steps")
        ));

        modules.add(new module(
                "Core Skills",
                "Develop essential skills and understanding",
                baseHours + 2,
                Arrays.asList("Key Concepts", "Practical Skills", "Problem Solving", "Best Practices")
        ));

        modules.add(new module(
                "Practical Application",
                "Apply your knowledge through hands-on projects",
                baseHours + 2,
                Arrays.asList("Project Planning", "Implementation", "Testing", "Debugging")
        ));

        modules.add(new module(
                "Advanced Techniques",
                "Master advanced concepts and optimization",
                baseHours + 3,
                Arrays.asList("Advanced Features", "Performance", "Architecture", "Real-world Applications")
        ));

        return modules;
    }

    private int getBaseHours(String skillLevel) {
        switch (skillLevel.toUpperCase()) {
            case "BEGINNER":
                return 10;
            case "INTERMEDIATE":
                return 8;
            case "ADVANCED":
                return 6;
            default:
                return 8;
        }
    }

    private String generateTitle(String target, String name, String skill) {
        String who = (name == null || name.isBlank()) ? "Personalized" : name + "'s";
        String what = (target == null || target.isBlank()) ? "Learning Path" : target + " Learning Path";
        return who + " " + what + " (" + skill + ")";
    }

    private int skillToNumeric(String skill) {
        switch (skill.toUpperCase(Locale.ROOT)) {
            case "BEGINNER":
                return 1;
            case "INTERMEDIATE":
                return 3;
            case "ADVANCED":
                return 5;
            default:
                return 2;
        }
    }
}