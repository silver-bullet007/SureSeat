package com.seatsure.seatsure.event;

import java.time.LocalDateTime;

// A plain class (not a record - same Jackson polymorphic-typing lesson
// from Redis applies here too, though Kafka's JsonSerializer is generally
// more forgiving; we're keeping it consistent regardless) representing the
// FACT that a booking was confirmed. This is the "message" that gets
// published to Kafka - deliberately containing only what a downstream
// consumer (an email service, analytics, etc.) would actually need, not
// the full Booking entity.
public class BookingConfirmedEvent {
    private Long bookingId;
    private String userEmail;
    private String eventTitle;
    private String seatNumber;
    private LocalDateTime confirmedAt;

    public BookingConfirmedEvent() {
    }

    public BookingConfirmedEvent(Long bookingId, String userEmail, String eventTitle,
            String seatNumber, LocalDateTime confirmedAt) {
        this.bookingId = bookingId;
        this.userEmail = userEmail;
        this.eventTitle = eventTitle;
        this.seatNumber = seatNumber;
        this.confirmedAt = confirmedAt;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}