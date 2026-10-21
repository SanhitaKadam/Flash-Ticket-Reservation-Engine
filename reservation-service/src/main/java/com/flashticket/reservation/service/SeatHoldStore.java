package com.flashticket.reservation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The only class that talks to Redis. Everything else asks this class.
 *
 * Redis keys:
 *   seat:hold:{eventId}:{seatId}  -> reservationId   (TTL = hold window)  "who is holding this seat"
 *   reservation:{reservationId}   -> JSON of Hold    (TTL = hold window)  "what did they hold"
 */
@Component
public class SeatHoldStore {

    public record Hold(String id, Long userId, Long eventId, List<Long> seatIds, Instant expiresAt) {}

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final RedisScript<Long> holdScript =
            RedisScript.of(new ClassPathResource("redis/hold_seats.lua"), Long.class);
    private final RedisScript<Long> releaseScript =
            RedisScript.of(new ClassPathResource("redis/release_seats.lua"), Long.class);

    public SeatHoldStore(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    static String seatKey(Long eventId, Long seatId) { return "seat:hold:" + eventId + ":" + seatId; }
    static String reservationKey(String id) { return "reservation:" + id; }

    /** Atomically holds ALL seats or NONE. Returns null on success, else the id of the first seat already held. */
    public Long tryHold(Hold hold, long ttlSeconds) {
        List<String> keys = new ArrayList<>();
        for (Long seatId : hold.seatIds()) keys.add(seatKey(hold.eventId(), seatId));
        keys.add(reservationKey(hold.id()));

        Long result = redis.execute(holdScript, keys, hold.id(), String.valueOf(ttlSeconds), toJson(hold));
        if (result == null) throw new IllegalStateException("Redis returned no result for hold script");
        if (result == 0L) return null;
        return hold.seatIds().get(result.intValue() - 1);
    }

    public Optional<Hold> find(String reservationId) {
        String json = redis.opsForValue().get(reservationKey(reservationId));
        if (json == null) return Optional.empty();
        try {
            return Optional.of(mapper.readValue(json, Hold.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Corrupt reservation in Redis: " + reservationId, e);
        }
    }

    /** True only if every seat key still exists AND still belongs to this reservation. */
    public boolean seatsStillHeldBy(Hold hold) {
        List<String> keys = hold.seatIds().stream().map(id -> seatKey(hold.eventId(), id)).toList();
        List<String> values = redis.opsForValue().multiGet(keys);
        return values != null && values.stream().allMatch(hold.id()::equals);
    }

    /** Which of these seats are currently held? One MGET round trip for the whole seat map. */
    public Set<Long> heldSeatIds(Long eventId, Collection<Long> seatIds) {
        List<Long> ids = new ArrayList<>(seatIds);
        if (ids.isEmpty()) return Set.of();
        List<String> keys = ids.stream().map(id -> seatKey(eventId, id)).toList();
        List<String> values = redis.opsForValue().multiGet(keys);
        Set<Long> held = new HashSet<>();
        if (values != null) {
            for (int i = 0; i < ids.size(); i++) {
                if (values.get(i) != null) held.add(ids.get(i));
            }
        }
        return held;
    }

    /** Cancel: frees the seats (only those still owned by this reservation). */
    public void release(Hold hold) {
        List<String> keys = new ArrayList<>();
        for (Long seatId : hold.seatIds()) keys.add(seatKey(hold.eventId(), seatId));
        keys.add(reservationKey(hold.id()));
        redis.execute(releaseScript, keys, hold.id());
    }

    /** After a successful booking: the reservation is used up. Seat keys are left to expire on their own. */
    public void deleteReservation(String reservationId) {
        redis.delete(reservationKey(reservationId));
    }

    private String toJson(Hold hold) {
        try {
            return mapper.writeValueAsString(hold);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialise hold", e);
        }
    }
}
