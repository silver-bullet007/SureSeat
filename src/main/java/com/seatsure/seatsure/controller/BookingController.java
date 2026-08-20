package com.seatsure.seatsure.controller;

import com.seatsure.seatsure.dto.BookingResponse;
import com.seatsure.seatsure.dto.CreateBookingRequest;
import com.seatsure.seatsure.dto.HoldResponse;
import com.seatsure.seatsure.security.RateLimiterService;
import com.seatsure.seatsure.security.SecurityUtils;
import com.seatsure.seatsure.service.BookingService;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final RateLimiterService rateLimiterService;

    public BookingController(BookingService bookingService, RateLimiterService rateLimiterService) {
        this.bookingService = bookingService;
        this.rateLimiterService = rateLimiterService;
    }

    // The real-world flow starts here: hold a seat. Rate limited, since
    // this is the endpoint most exposed to a real traffic spike (a hot
    // event's tickets going live) and the most valuable to protect from abuse.
    @PostMapping("/hold")
    public ResponseEntity<?> holdSeat(@Valid @RequestBody CreateBookingRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        Bucket bucket = rateLimiterService.resolveBucket(userEmail);

        // tryConsume attempts to remove one token, returning true/false
        // immediately - it never blocks or waits, unlike the pessimistic
        // database locks we built in Stage 2. This is a deliberate design
        // difference: a rate limiter should fail fast, not queue requests.
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many booking attempts. Please wait a moment and try again.");
        }

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