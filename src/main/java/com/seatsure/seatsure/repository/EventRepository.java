package com.seatsure.seatsure.repository;

import com.seatsure.seatsure.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByVenue(String venue);
}
