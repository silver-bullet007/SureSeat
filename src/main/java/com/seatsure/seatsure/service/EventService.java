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

    @Transactional
    public List<SeatResponse> addSeats(Long event_id, CreateSeatsRequest request) {
        // method will allow the organizer to add the seats that would be present for an
        // event , default to AVAILABLE

        // first lets do a confirmation and check if the event is present
        Event event = eventRepository.findById(event_id)
                .orElseThrow(() -> new NoSuchElementException("No event found with that id " + event_id));

        // if we come here it means that the event exists and the organizer can add the
        // seats to the Event object
        // each event object has its seats as a List of seat objects List<Seat>
        List<Seat> addedSeats = request.seatNumbers().stream().map(seat_no -> {
            // for each seat number create a new seat object
            Seat seat = new Seat();
            seat.setSeatNumber(seat_no);
            seat.setEvent(event);
            return seat;
        }).toList();

        event.getSeats().addAll(addedSeats);
        eventRepository.save(event);

        return addedSeats.stream()
                .map(s -> new SeatResponse(s.getId(), s.getSeatNumber(), s.getStatus().name()))
                .toList();

    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("No event found with id " + eventId));

        return event.getSeats().stream()
                .map(s -> new SeatResponse(s.getId(), s.getSeatNumber(), s.getStatus().name()))
                .toList();
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