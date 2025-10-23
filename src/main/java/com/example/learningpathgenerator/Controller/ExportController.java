package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.model.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;  // Add this import
import java.util.List;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @PostMapping(value = "/learning-path/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportLearningPathCsv(@RequestBody LearningPath path) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Get current timestamp for filename
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        try (var writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            // Header with timestamp
            writer.write("Learning Path Export - Generated: " +
                    now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.newLine();
            writer.newLine();

            // Learning path details
            writer.write("Title,User,Skill Level,Weekly Hours,Estimated Weeks,Total Hours");
            writer.newLine();
            writer.write(String.format("%s,%s,%s,%d,%d,%d",
                    escape(path.getTitle()),
                    escape(path.getUserName()),
                    path.getSkillLevel(),
                    path.getWeeklyHours(),
                    path.getEstimatedWeeks(),
                    path.getTotalHours()));
            writer.newLine();
            writer.newLine();

            // Modules table header
            writer.write("Module Title,Description,Hours,Resources");
            writer.newLine();

            List<module> modules = path.getmodules();
            if (modules != null) {
                for (module m : modules) {
                    String resources = String.join(" | ",
                            m.getResources() == null ? List.of() : m.getResources());
                    writer.write(String.format("%s,%s,%d,%s",
                            escape(m.getTitle()),
                            escape(m.getDescription()),
                            m.getHours(),
                            escape(resources)));
                    writer.newLine();
                }
            }
            writer.flush();
        }

        byte[] csvBytes = out.toByteArray();

        // Create filename with timestamp
        String filename = String.format("learning-path-%s-%s.csv",
                path.getUserName() != null ? path.getUserName() : "user",
                timestamp);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(filename)
                .build());
        headers.setContentLength(csvBytes.length);

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}