package com.seatsure.seatsure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingEventProducer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventProducer.class);
    private static final String TOPIC = "booking-confirmed";

    // KafkaTemplate is Spring Kafka's core class for SENDING messages -
    // auto-configured as a bean based on the producer settings we added
    // to application.yml, the same way JpaRepository implementations are
    // auto-provided based on our datasource config.
    private final KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate;

    public BookingEventProducer(KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        // send() is asynchronous - it returns immediately with a
        // CompletableFuture representing the eventual send result, rather
        // than blocking the calling thread until Kafka acknowledges receipt.
        // This is EXACTLY the "fire and forget, don't slow down the real
        // request" property we discussed as Kafka's whole point.
        kafkaTemplate.send(TOPIC, event.getBookingId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish booking confirmed event for booking {}",
                                event.getBookingId(), ex);
                    } else {
                        log.info("Published booking confirmed event for booking {}", event.getBookingId());
                    }
                });
    }
}