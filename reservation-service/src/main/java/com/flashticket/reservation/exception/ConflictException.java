package com.flashticket.reservation.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
