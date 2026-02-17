package com.example.usecase24;

import java.time.LocalDateTime;

public record Notification(String id, String title, String message,
        NotificationType type, boolean read, LocalDateTime timestamp) {

    public Notification withRead(boolean newRead) {
        return new Notification(id, title, message, type, newRead, timestamp);
    }
}
