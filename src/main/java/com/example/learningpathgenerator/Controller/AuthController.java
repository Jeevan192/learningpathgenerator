package com.example.learningpathgenerator.Controller;

import com.example.learningpathgenerator.dto.AuthRequest;
import com.example.learningpathgenerator.dto.AuthResponse;
import com.example.learningpathgenerator.dto.RegisterRequest;
import com.example.learningpathgenerator.entity.Role;
import com.example.learningpathgenerator.entity.User;
import com.example.learningpathgenerator.repository.UserRepository;
import com.example.learningpathgenerator.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Secret key for admin registration - can be configured in application.properties
    @Value("${admin.secret.key:ADMIN_SECRET_2024}")
    private String adminSecretKey;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build());
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request) {
        // Validate admin secret key
        if (request.getAdminSecretKey() == null || !request.getAdminSecretKey().equals(adminSecretKey)) {
            return ResponseEntity.status(403).body("Invalid admin secret key");
        }

        // Validate security question and answer
        if (request.getSecurityQuestion() == null || request.getSecurityAnswer() == null) {
            return ResponseEntity.badRequest().body("Security question and answer are required for admin registration");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .securityQuestion(request.getSecurityQuestion())
                .securityAnswer(passwordEncoder.encode(request.getSecurityAnswer().toLowerCase()))
                .role(Role.ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String username = request.get("username");

        User user = null;
        if (email != null && !email.isEmpty()) {
            user = userRepository.findByEmail(email).orElse(null);
        } else if (username != null && !username.isEmpty()) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Return security question if exists, otherwise return reset token directly
        if (user.getSecurityQuestion() != null) {
            return ResponseEntity.ok(Map.of(
                "message", "Please answer your security question",
                "securityQuestion", user.getSecurityQuestion(),
                "resetToken", resetToken
            ));
        }

        return ResponseEntity.ok(Map.of(
            "message", "Password reset token generated",
            "resetToken", resetToken
        ));
    }

    @PostMapping("/verify-security-answer")
    public ResponseEntity<?> verifySecurityAnswer(@RequestBody Map<String, String> request) {
        String resetToken = request.get("resetToken");
        String securityAnswer = request.get("securityAnswer");

        User user = userRepository.findByResetToken(resetToken).orElse(null);

        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired reset token");
        }

        if (!passwordEncoder.matches(securityAnswer.toLowerCase(), user.getSecurityAnswer())) {
            return ResponseEntity.badRequest().body("Incorrect security answer");
        }

        return ResponseEntity.ok(Map.of("message", "Security answer verified", "resetToken", resetToken));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String resetToken = request.get("resetToken");
        String newPassword = request.get("newPassword");

        User user = userRepository.findByResetToken(resetToken).orElse(null);

        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired reset token");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
