package com.collabtask.authservice.exception;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException() {
        super("User role not found in system.");
    }
}
