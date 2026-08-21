package com.seatsure.seatsure.dto;

import java.time.LocalDateTime;

public record EventResponse(
                Long id,
                String title,
                String description,
                String venue,
                LocalDateTime eventTime,
                String organizerName,
                int totalSeats,
                int availableSeats) {
}