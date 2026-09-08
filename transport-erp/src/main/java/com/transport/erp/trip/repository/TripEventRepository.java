package com.transport.erp.trip.repository;

import com.transport.erp.trip.domain.TripEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripEventRepository extends JpaRepository<TripEvent, UUID> {

    List<TripEvent> findByTripIdOrderByEventTimestampAsc(UUID tripId);
}
