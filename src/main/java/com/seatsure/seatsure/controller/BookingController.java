package com.seatsure.seatsure.controller;

import com.seatsure.seatsure.dto.BookingResponse;
import com.seatsure.seatsure.dto.CreateBookingRequest;
import com.seatsure.seatsure.dto.HoldResponse;
import com.seatsure.seatsure.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // The real-world flow starts here: hold a seat.
    @PostMapping("/hold")
    public ResponseEntity<HoldResponse> holdSeat(@Valid @RequestBody CreateBookingRequest request) {
        HoldResponse response = bookingService.holdSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Then confirm it (simulating successful payment).
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long bookingId) {
        BookingResponse response = bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    // Kept from Stage 2, for comparing locking strategies directly.
    @PostMapping
    public ResponseEntity<BookingResponse> bookSeat(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.bookSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/optimistic")
    public ResponseEntity<BookingResponse> bookSeatOptimistic(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.bookSeatOptimistic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}