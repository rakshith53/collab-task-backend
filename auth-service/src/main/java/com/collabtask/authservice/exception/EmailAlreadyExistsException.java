package com.collabtask.authservice.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String field) {
        super("A user with this " + field + " already exists.");
    }
}
