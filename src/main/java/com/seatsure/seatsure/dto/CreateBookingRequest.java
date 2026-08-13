package com.seatsure.seatsure.dto;

import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Seat ID is required")
        Long seatId
) {
}