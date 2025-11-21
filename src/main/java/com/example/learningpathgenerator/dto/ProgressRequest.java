package com.example.learningpathgenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressRequest {
    private String username;
    private List<Integer> completedModules = new ArrayList<>();
    private Integer currentModule = 0;
    private Double overallProgress = 0.0;
    private Integer totalModules = 0;
}