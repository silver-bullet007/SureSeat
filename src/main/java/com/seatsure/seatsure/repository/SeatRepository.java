package com.seatsure.seatsure.repository;

import com.seatsure.seatsure.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEventIdAndStatus(Long eventId, Seat.SeatStatus status);
}