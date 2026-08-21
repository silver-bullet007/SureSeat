package com.seatsure.seatsure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    // @KafkaListener does the heavy lifting: Spring sets up a background
    // thread that continuously polls the given topic, and calls this
    // method automatically every time a new message arrives - we never
    // manually poll or manage a consumer loop ourselves.
    @KafkaListener(topics = "booking-confirmed", groupId = "seatsure-group")
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        // In a real system, this is where you'd call an actual email/SMS
        // provider. We simulate it with a log line and a short artificial
        // delay, to make the async decoupling from the booking request
        // visible and provable.
        log.info("Simulating confirmation email to {} for booking #{} - {} seat {}",
                event.getUserEmail(), event.getBookingId(),
                event.getEventTitle(), event.getSeatNumber());

        try {
            Thread.sleep(2000); // simulate a slow external email provider
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Confirmation email sent for booking #{}", event.getBookingId());
    }
}