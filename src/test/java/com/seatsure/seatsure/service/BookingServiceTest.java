package com.seatsure.seatsure.service;

import com.seatsure.seatsure.dto.CreateBookingRequest;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class) tells JUnit 5 to activate Mockito's
// annotation processing (@Mock, @InjectMocks below) for this test class -
// without it, those annotations would just be inert and do nothing.
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    // @Mock creates a fake, controllable stand-in for each dependency -
    // none of these ever touch a real database or Redis. We tell each
    // mock exactly what to return when called, entirely within this test.
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

    // @InjectMocks creates a REAL BookingService instance, but automatically
    // wires in the @Mock objects above wherever BookingService's constructor
    // expects those types - no Spring context needed to do this.
    @InjectMocks
    private BookingService bookingService;

    private Seat heldSeat;

    @BeforeEach
    void setUp() {
        heldSeat = new Seat();
        heldSeat.setId(1L);
        heldSeat.setSeatNumber("A1");
        heldSeat.setStatus(Seat.SeatStatus.HELD); // already held, NOT available
    }

    @Test
    void holdSeat_shouldThrow_whenSeatIsNotAvailable() {
        // Arrange: tell the mock repository "when asked for seat 1 with a
        // lock, return our already-HELD seat" - simulating the exact
        // real-world scenario this business rule needs to reject.
        when(seatRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(heldSeat));

        CreateBookingRequest request = new CreateBookingRequest(1L);

        // Act + Assert: calling holdSeat() with this request should throw
        // IllegalStateException, and the exception message should mention
        // the seat number - verifying both the failure type AND the
        // specific, correct business rule that caused it.
        assertThatThrownBy(() -> bookingService.holdSeat(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("A1")
                .hasMessageContaining("not available");
    }
}