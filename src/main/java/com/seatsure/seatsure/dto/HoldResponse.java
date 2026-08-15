package com.seatsure.seatsure.dto;

import java.time.LocalDateTime;

// Extends what BookingResponse shows with the hold's expiry time, so a
// client knows exactly how long they have to complete payment.
public record HoldResponse(
        Long id,
        String eventTitle,
        String seatNumber,
        String userEmail,
        String status,
        LocalDateTime expiresAt) {
}