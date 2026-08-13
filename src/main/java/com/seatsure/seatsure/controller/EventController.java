package com.seatsure.seatsure.controller;

import com.seatsure.seatsure.dto.CreateEventRequest;
import com.seatsure.seatsure.dto.CreateSeatsRequest;
import com.seatsure.seatsure.dto.EventResponse;
import com.seatsure.seatsure.dto.SeatResponse;
import com.seatsure.seatsure.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping("/{eventId}/seats")
    public ResponseEntity<List<SeatResponse>> addSeats(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateSeatsRequest request) {
        List<SeatResponse> response = eventService.addSeats(eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{eventId}/seats")
    public ResponseEntity<List<SeatResponse>> getSeats(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getSeatsForEvent(eventId));
    }
}