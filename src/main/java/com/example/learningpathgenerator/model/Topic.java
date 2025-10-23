package com.example.learningpathgenerator.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "topics")
public class Topic {

    @Id
    private String id;

    private String name;

    private String category;

    private int difficulty; // 1 (easy) - 5 (hard)

    private int recommendedHours;

    @ElementCollection
    @CollectionTable(name = "topic_resources", joinColumns = @JoinColumn(name = "topic_id"))
    @Column(name = "resource")
    private List<String> resources = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "topic_prerequisites", joinColumns = @JoinColumn(name = "topic_id"))
    @Column(name = "prerequisite")
    private List<String> prerequisites = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "topic_tags", joinColumns = @JoinColumn(name = "topic_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    public Topic() {
    }

    public Topic(String id, String name, String category, int difficulty, int recommendedHours, List<String> resources) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.difficulty = difficulty;
        this.recommendedHours = recommendedHours;
        this.resources = resources;
    }

    public Topic(String id, String name, String category, int difficulty, int recommendedHours, List<String> resources, List<String> prerequisites, Set<String> tags) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.difficulty = difficulty;
        this.recommendedHours = recommendedHours;
        this.resources = resources;
        this.prerequisites = prerequisites;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getRecommendedHours() {
        return recommendedHours;
    }

    public List<String> getResources() {
        return resources;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setRecommendedHours(int recommendedHours) {
        this.recommendedHours = recommendedHours;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}