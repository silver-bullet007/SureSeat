package com.seatsure.seatsure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "event_id", "seat_number" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatNumber; // e.g. "A1", "B12"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    // This is the "owning side" of the relationship - this column actually
    // exists in the seats table as event_id (a foreign key).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Optimistic locking version - this is our FIRST concurrency control tool.
    // Every UPDATE increments this automatically; if two threads try to update
    // the same row with a stale version number, the second one fails loudly
    // instead of silently overwriting the first. More on this in Stage 2.
    @Version
    private Long version;

    public enum SeatStatus {
        AVAILABLE, HELD, BOOKED
    }
}