package com.collabtask.authservice.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String field) {
        super("A user with this " + field + " already exists.");
    }
}
