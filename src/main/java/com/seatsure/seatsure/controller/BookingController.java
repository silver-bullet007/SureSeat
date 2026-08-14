package com.seatsure.seatsure.controller;

import com.seatsure.seatsure.dto.BookingResponse;
import com.seatsure.seatsure.dto.CreateBookingRequest;
import com.seatsure.seatsure.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> bookSeat(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.bookSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Same booking flow, but using optimistic locking - for direct
    // side-by-side comparison against the pessimistic version above.
    @PostMapping("/optimistic")
    public ResponseEntity<BookingResponse> bookSeatOptimistic(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.bookSeatOptimistic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}