package com.seatsure.seatsure.service;

import com.seatsure.seatsure.dto.CreateEventRequest;
import com.seatsure.seatsure.dto.EventResponse;
import com.seatsure.seatsure.entity.Event;
import com.seatsure.seatsure.entity.Seat;
import com.seatsure.seatsure.entity.User;
import com.seatsure.seatsure.repository.EventRepository;
import com.seatsure.seatsure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.seatsure.seatsure.dto.CreateSeatsRequest;
import com.seatsure.seatsure.dto.SeatResponse;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // Constructor injection - Spring sees this constructor and automatically
    // supplies both repository beans. No @Autowired needed on the constructor
    // itself when there's only one constructor - Spring infers it.
    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        User organizer = userRepository.findById(request.organizerId())
                .orElseThrow(() -> new NoSuchElementException(
                        "No user found with id " + request.organizerId()));

        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setVenue(request.venue());
        event.setEventTime(request.eventTime());
        event.setOrganizer(organizer);

        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No event found with id " + id));
        return toResponse(event);
    }

    // Converts an Event entity into the DTO we actually expose via the API.
    // This is the ONE place that translation logic lives.
    private EventResponse toResponse(Event event) {
        int total = event.getSeats().size();
        long available = event.getSeats().stream()
                .filter(s -> s.getStatus() == Seat.SeatStatus.AVAILABLE)
                .count();

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getVenue(),
                event.getEventTime(),
                event.getOrganizer().getFullName(),
                total,
                (int) available);
    }
}