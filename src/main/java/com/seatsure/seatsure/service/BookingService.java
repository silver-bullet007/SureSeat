package com.seatsure.seatsure.service;

import com.seatsure.seatsure.dto.BookingResponse;
import com.seatsure.seatsure.dto.CreateBookingRequest;
import com.seatsure.seatsure.dto.HoldResponse;
import com.seatsure.seatsure.entity.Booking;
import com.seatsure.seatsure.entity.Seat;
import com.seatsure.seatsure.entity.User;
import com.seatsure.seatsure.repository.BookingRepository;
import com.seatsure.seatsure.repository.SeatRepository;
import com.seatsure.seatsure.repository.UserRepository;
import com.seatsure.seatsure.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final long HOLD_DURATION_MINUTES = 5;

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

    // Step 1 of the real flow: hold a seat for HOLD_DURATION_MINUTES.
    @Transactional
    public HoldResponse holdSeat(CreateBookingRequest request) {
        Seat seat = seatRepository.findByIdForUpdate(request.seatId())
                .orElseThrow(() -> new NoSuchElementException("No seat found with id " + request.seatId()));

        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is not available");
        }

        User user = userRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found in database"));

        seat.setStatus(Seat.SeatStatus.HELD);
        seatRepository.save(seat);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(seat.getEvent());
        booking.setSeat(seat);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES));

        Booking saved = bookingRepository.save(booking);

        return new HoldResponse(
                saved.getId(),
                seat.getEvent().getTitle(),
                seat.getSeatNumber(),
                user.getEmail(),
                saved.getStatus().name(),
                saved.getExpiresAt());
    }

    // Step 2: confirm a held booking (simulating successful payment).
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("No booking found with id " + bookingId));

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Booking is not in a confirmable state (status: " + booking.getStatus() + ")");
        }

        if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This hold has expired. Please book the seat again.");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        Seat seat = booking.getSeat();
        seat.setStatus(Seat.SeatStatus.BOOKED);
        seatRepository.save(seat);
        Booking saved = bookingRepository.save(booking);

        return new BookingResponse(
                saved.getId(),
                seat.getEvent().getTitle(),
                seat.getSeatNumber(),
                booking.getUser().getEmail(),
                saved.getStatus().name());
    }

    // Step 3: the background job. Runs on a fixed schedule, finds any
    // PENDING booking whose hold has expired, and releases the seat.
    @Scheduled(fixedRate = 60000) // runs every 60,000 ms = every 1 minute
    @Transactional
    public void releaseExpiredHolds() {
        List<Booking> expired = bookingRepository.findExpiredPendingBookings(
                Booking.BookingStatus.PENDING, LocalDateTime.now());

        for (Booking booking : expired) {
            booking.setStatus(Booking.BookingStatus.EXPIRED);
            Seat seat = booking.getSeat();
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seatRepository.save(seat);
            bookingRepository.save(booking);
            log.info("Released expired hold: booking {} seat {}", booking.getId(), seat.getSeatNumber());
        }
    }

    // ===== Kept from Stage 2, for comparing pessimistic vs optimistic locking
    // =====

    @Transactional
    public BookingResponse bookSeat(CreateBookingRequest request) {
        Seat seat = seatRepository.findByIdForUpdate(request.seatId())
                .orElseThrow(() -> new NoSuchElementException("No seat found with id " + request.seatId()));

        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is not available");
        }

        User user = userRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found in database"));

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

    @Transactional
    public BookingResponse bookSeatOptimistic(CreateBookingRequest request) {
        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new NoSuchElementException("No seat found with id " + request.seatId()));

        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is not available");
        }

        User user = userRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found in database"));

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

    private BookingResponse buildResponse(Booking saved, Seat seat, User user) {
        return new BookingResponse(
                saved.getId(),
                seat.getEvent().getTitle(),
                seat.getSeatNumber(),
                user.getEmail(),
                saved.getStatus().name());
    }
}