package com.seatsure.seatsure.repository;

import com.seatsure.seatsure.entity.Event;
import com.seatsure.seatsure.entity.Seat;
import com.seatsure.seatsure.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
// Import a minimal, in-memory CacheManager just for this test's context -
// @DataJpaTest doesn't load our real, Redis-backed CacheConfig, but
// @EnableCaching on the main app class still gets picked up and requires
// SOME CacheManager bean to exist. We don't need real caching behavior
// for this test - just enough to satisfy that requirement.
@Import(SeatRepositoryIntegrationTest.TestCacheConfig.class)
class SeatRepositoryIntegrationTest {

    @TestConfiguration
    static class TestCacheConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByIdForUpdate_shouldReturnSeat_whenSeatExists() {
        User organizer = new User();
        organizer.setEmail("organizer@integrationtest.com");
        organizer.setPassword("hashed-placeholder");
        organizer.setFullName("Integration Test Organizer");
        organizer.setRole(User.Role.ORGANIZER);
        organizer = userRepository.save(organizer);

        Event event = new Event();
        event.setTitle("Integration Test Event");
        event.setVenue("Test Venue");
        event.setEventTime(LocalDateTime.now().plusDays(1));
        event.setOrganizer(organizer);
        event = eventRepository.save(event);

        Seat seat = new Seat();
        seat.setSeatNumber("Z9");
        seat.setEvent(event);
        Seat savedSeat = seatRepository.save(seat);

        Optional<Seat> found = seatRepository.findByIdForUpdate(savedSeat.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSeatNumber()).isEqualTo("Z9");
        assertThat(found.get().getStatus()).isEqualTo(Seat.SeatStatus.AVAILABLE);
    }
}