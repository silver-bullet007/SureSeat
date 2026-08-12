package com.seatsure.seatsure.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank(message = "Title is required") String title,

        String description,

        @NotBlank(message = "Venue is required") String venue,

        @NotNull(message = "Event time is required") @Future(message = "Event time must be in the future") LocalDateTime eventTime,

        @NotNull(message = "Organizer ID is required") Long organizerId) {
}