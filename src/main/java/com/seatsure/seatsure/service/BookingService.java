package com.seatsure.seatsure.service;

import com.seatsure.seatsure.dto.BookingResponse;
import com.seatsure.seatsure.dto.CreateBookingRequest;
import com.seatsure.seatsure.entity.Booking;
import com.seatsure.seatsure.entity.Seat;
import com.seatsure.seatsure.entity.User;
import com.seatsure.seatsure.repository.BookingRepository;
import com.seatsure.seatsure.repository.SeatRepository;
import com.seatsure.seatsure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class BookingService {

    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public BookingService(SeatRepository seatRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository) {
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public BookingResponse bookSeat(CreateBookingRequest request) {
        // Step 1: READ the seat's current status, WITH a row lock (FOR UPDATE).
        // Any other transaction trying to lock this same row now BLOCKS here
        // until we commit or roll back - no more racing on the read.
        Seat seat = seatRepository.findByIdForUpdate(request.seatId())
                .orElseThrow(() -> new NoSuchElementException("No seat found with id " + request.seatId()));

        // Step 2: CHECK if it's available. By the time a second, previously
        // blocked request reaches this line, it is guaranteed to see the
        // TRUE current status - BOOKED - because it could only proceed
        // past Step 1 after the first transaction fully committed.
        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is not available");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException("No user found with id " + request.userId()));

        // Step 3: WRITE - safe now, because we hold the lock and just
        // verified the status ourselves, with no other transaction able
        // to have changed it in between.
        seat.setStatus(Seat.SeatStatus.BOOKED);
        seatRepository.save(seat);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(seat.getEvent());
        booking.setSeat(seat);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);

        return buildResponse(saved, seat, user);

    }

    // ===== OPTIMISTIC LOCKING VARIANT =====
    // No FOR UPDATE, no blocking. Both concurrent requests can read and
    // proceed freely. The @Version field on Seat is what saves us: Hibernate
    // includes "AND version = ?" in the UPDATE's WHERE clause automatically.
    // If another transaction already committed a change (bumping the version),
    // this UPDATE affects ZERO rows, and Hibernate throws
    // ObjectOptimisticLockingFailureException - which we catch and translate
    // into the same clean 409 response.
    @Transactional
    public BookingResponse bookSeatOptimistic(CreateBookingRequest request) {
        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new NoSuchElementException("No seat found with id " + request.seatId()));

        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is not available");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException("No user found with id " + request.userId()));

        seat.setStatus(Seat.SeatStatus.BOOKED);
        seatRepository.save(seat); // <-- this is where a version mismatch throws, if it's going to

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(seat.getEvent());
        booking.setSeat(seat);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);

        return buildResponse(saved, seat, user);
    }

    private BookingResponse buildResponse(Booking saved, Seat seat, User user) {
        return new BookingResponse(
                saved.getId(),
                seat.getEvent().getTitle(),
                seat.getSeatNumber(),
                user.getEmail(),
                saved.getStatus().name());
    }
}