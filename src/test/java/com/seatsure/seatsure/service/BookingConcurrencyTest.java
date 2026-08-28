package com.seatsure.seatsure.service;

import com.seatsure.seatsure.dto.CreateBookingRequest;
import com.seatsure.seatsure.entity.Event;
import com.seatsure.seatsure.entity.Seat;
import com.seatsure.seatsure.entity.User;
import com.seatsure.seatsure.repository.EventRepository;
import com.seatsure.seatsure.repository.SeatRepository;
import com.seatsure.seatsure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

// Full @SpringBootTest - we genuinely need the real application context here
// (real transactions, real locking, real thread pool behavior) - this is
// deliberately NOT a narrow test slice like @DataJpaTest, since we're
// proving behavior that only emerges from the full, real system under load.
@SpringBootTest
class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void holdSeat_underConcurrentLoad_onlyOneRequestSucceeds() throws InterruptedException {
        // Arrange: one seat, one event, and 20 DIFFERENT users all about to
        // race for that single seat simultaneously.
        User organizer = userRepository.save(buildUser("organizer@loadtest.com", User.Role.ORGANIZER));

        Event event = new Event();
        event.setTitle("Load Test Concert");
        event.setVenue("Load Test Arena");
        event.setEventTime(LocalDateTime.now().plusDays(1));
        event.setOrganizer(organizer);
        event = eventRepository.save(event);

        Seat seat = new Seat();
        seat.setSeatNumber("LOAD1");
        seat.setEvent(event);
        Seat savedSeat = seatRepository.save(seat);

        int numberOfConcurrentUsers = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentUsers);
        CountDownLatch readyLatch = new CountDownLatch(numberOfConcurrentUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<User> users = new java.util.ArrayList<>();
        for (int i = 0; i < numberOfConcurrentUsers; i++) {
            users.add(userRepository.save(buildUser("loadtestuser" + i + "@test.com", User.Role.USER)));
        }

        // Act: submit 20 tasks, each simulating one user trying to hold
        // the SAME seat, all released to fire at the exact same instant.
        for (User user : users) {
            executor.submit(() -> {
                try {
                    // Each thread needs its OWN authenticated security
                    // context, since SecurityContextHolder is thread-local -
                    // simulating 20 genuinely different logged-in users.
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    user.getEmail(), null,
                                    List.of(new SimpleGrantedAuthority("ROLE_USER"))));

                    readyLatch.countDown(); // signal "I'm ready and waiting"
                    startLatch.await(); // block here until released simultaneously

                    bookingService.holdSeat(new CreateBookingRequest(savedSeat.getId()));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            });
        }

        readyLatch.await(); // wait until all 20 threads are loaded and waiting
        startLatch.countDown(); // release all 20 at the exact same moment
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // Assert: exactly ONE of the 20 simultaneous attempts succeeded,
        // and the other 19 failed cleanly - proving the pessimistic lock
        // correctly serialized access to this one contested row under
        // genuine concurrent load, not just our earlier two-request test.
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(19);
    }

    private User buildUser(String email, User.Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashed-placeholder");
        user.setFullName("Load Test User");
        user.setRole(role);
        return user;
    }
}