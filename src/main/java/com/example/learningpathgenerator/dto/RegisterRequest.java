package com.example.learningpathgenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String password;

    // For admin registration with security
    private String securityQuestion;
    private String securityAnswer;
    private String adminSecretKey;  // Secret key required for admin registration
}
