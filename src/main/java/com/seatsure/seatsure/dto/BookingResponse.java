package com.seatsure.seatsure.dto;

public record BookingResponse(
        Long id,
        String eventTitle,
        String seatNumber,
        String userEmail,
        String status
) {
}