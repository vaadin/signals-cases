package com.example.signals;

/**
 * Information about an active user session.
 */
public record UserInfo(String username, long sessionStartTime) {
    public UserInfo(String username) {
        this(username, System.currentTimeMillis());
    }
}
