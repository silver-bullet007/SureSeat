package com.seatsure.seatsure.dto;

public record AuthResponse(
        String token,
        String email,
        String fullName,
        String role) {
}