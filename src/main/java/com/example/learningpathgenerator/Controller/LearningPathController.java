package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.dto.GenerateRequest;
import com.example.learningpathgenerator.model.LearningPath;
import com.example.learningpathgenerator.Service.LearningPathService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-path")
public class LearningPathController {

    private final LearningPathService service;

    public LearningPathController(LearningPathService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<LearningPath> generate(@RequestBody GenerateRequest request) {
        LearningPath path = service.generatePath(request);
        return ResponseEntity.ok(path);
    }

    // Helpful GET for browser and quick inspection: returns an example payload and instructions
    @GetMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> generateInfo() {
        Map<String, Object> example = new HashMap<>();
        example.put("name", "Jeevan");
        example.put("skillLevel", "BEGINNER");
        example.put("interests", Arrays.asList("web", "algorithms"));
        example.put("weeklyHours", 6);
        example.put("target", "backend developer");

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "This endpoint accepts POST requests. Send JSON as shown in the 'example' field.");
        resp.put("example", example);

        return ResponseEntity.ok(resp);
    }
}