package com.flashticket.reservation.exception;

public class ReservationExpiredException extends RuntimeException {
    public ReservationExpiredException(String message) { super(message); }
}
