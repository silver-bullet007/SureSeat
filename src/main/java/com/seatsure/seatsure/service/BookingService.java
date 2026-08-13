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
        // Step 1: READ the seat's current status
        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new NoSuchElementException("No seat found with id " + request.seatId()));

        // Step 2: CHECK if it's available
        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is not available");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException("No user found with id " + request.userId()));

        // Step 3: WRITE - mark it booked and create the booking record
        // <-- THE PROBLEM: between Step 2's check and this write, another
        // thread could ALSO have passed its own Step 2 check, since both
        // read the seat as AVAILABLE before either wrote anything back.
        seat.setStatus(Seat.SeatStatus.BOOKED);
        seatRepository.save(seat);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(seat.getEvent());
        booking.setSeat(seat);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);

        return new BookingResponse(
                saved.getId(),
                seat.getEvent().getTitle(),
                seat.getSeatNumber(),
                user.getEmail(),
                saved.getStatus().name());
    }
}