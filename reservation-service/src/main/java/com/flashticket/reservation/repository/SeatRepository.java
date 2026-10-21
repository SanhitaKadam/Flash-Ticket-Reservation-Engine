package com.flashticket.reservation.repository;

import com.flashticket.reservation.domain.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByEventIdOrderByRowLabelAscSeatNumberAsc(Long eventId);

    // Phase 3: seats of one event by id (derived query: event.id = ? and id in (...))
    List<Seat> findByEventIdAndIdIn(Long eventId, Collection<Long> ids);

    // PESSIMISTIC_WRITE => SELECT ... FOR UPDATE. Ordering by id => consistent lock order => no deadlocks.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id in :ids and s.event.id = :eventId order by s.id")
    List<Seat> lockSeats(@Param("ids") Collection<Long> ids, @Param("eventId") Long eventId);
}
