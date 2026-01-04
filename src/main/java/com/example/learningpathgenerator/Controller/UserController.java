package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.entity.User;
import com.example.learningpathgenerator.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            log.info("Fetching profile for user: {}", username);

            User user = userService.findByUsername(username);
            if (user != null) {
                user.setPassword(null);
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error fetching user profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error fetching profile: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            log.info("Getting current user: {}", username);

            User user = userService.findByUsername(username);
            if (user != null) {
                user.setPassword(null);
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}

