package com.seatsure.seatsure.service;

import com.seatsure.seatsure.dto.CreateBookingRequest;
import com.seatsure.seatsure.entity.Booking;
import com.seatsure.seatsure.entity.Seat;
import com.seatsure.seatsure.event.BookingEventProducer;
import com.seatsure.seatsure.repository.BookingRepository;
import com.seatsure.seatsure.repository.SeatRepository;
import com.seatsure.seatsure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private SeatRepository seatRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private BookingEventProducer eventProducer;

    @InjectMocks
    private BookingService bookingService;

    private Seat heldSeat;

    @BeforeEach
    void setUp() {
        heldSeat = new Seat();
        heldSeat.setId(1L);
        heldSeat.setSeatNumber("A1");
        heldSeat.setStatus(Seat.SeatStatus.HELD);
    }

    @Test
    void holdSeat_shouldThrow_whenSeatIsNotAvailable() {
        when(seatRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(heldSeat));

        CreateBookingRequest request = new CreateBookingRequest(1L);

        assertThatThrownBy(() -> bookingService.holdSeat(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("A1")
                .hasMessageContaining("not available");
    }

    @Test
    void confirmBooking_shouldThrow_whenHoldHasExpired() {
        Booking expiredBooking = new Booking();
        expiredBooking.setId(99L);
        expiredBooking.setStatus(Booking.BookingStatus.PENDING);
        expiredBooking.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // already in the past

        when(bookingRepository.findById(99L)).thenReturn(Optional.of(expiredBooking));

        assertThatThrownBy(() -> bookingService.confirmBooking(99L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void confirmBooking_shouldThrow_whenBookingIsAlreadyConfirmed() {
        Booking alreadyConfirmed = new Booking();
        alreadyConfirmed.setId(100L);
        alreadyConfirmed.setStatus(Booking.BookingStatus.CONFIRMED); // not PENDING anymore

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(alreadyConfirmed));

        assertThatThrownBy(() -> bookingService.confirmBooking(100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in a confirmable state");
    }
}