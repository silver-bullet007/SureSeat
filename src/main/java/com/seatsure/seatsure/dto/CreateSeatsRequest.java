package com.seatsure.seatsure.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateSeatsRequest(
        @NotEmpty(message = "At least one seat number is required") List<String> seatNumbers // e.g. ["A1", "A2", "A3"]
) {
}