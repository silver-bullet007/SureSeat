package com.seatsure.seatsure.repository;

import com.seatsure.seatsure.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEventIdAndStatus(Long eventId, Seat.SeatStatus status);

    // @Lock tells Spring Data to add FOR UPDATE to this query's generated SQL.
    // We need @Query here because @Lock only applies to explicitly-written
    // queries, not the auto-generated findById() from JpaRepository.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdForUpdate(Long id);
}