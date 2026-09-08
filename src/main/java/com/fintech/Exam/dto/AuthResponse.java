package com.fintech.Exam.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
