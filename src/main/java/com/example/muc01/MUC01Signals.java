package com.example.muc01;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.vaadin.signals.shared.SharedListSignal;

/**
 * Application-scoped signals for MUC01: Shared Chat
 */
@Component
public class MUC01Signals {

    public record Message(String username, String author, String text,
            LocalDateTime timestamp) {
        public Message(String username, String author, String text) {
            this(username, author, text, LocalDateTime.now());
        }

        public String getFormattedTimestamp() {
            return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }

    private final SharedListSignal<Message> messagesSignal = new SharedListSignal<>(
            Message.class);

    public SharedListSignal<Message> getMessagesSignal() {
        return messagesSignal;
    }

    public void appendMessage(Message message) {
        messagesSignal.insertLast(message);
    }

    public void clearMessages() {
        messagesSignal.clear();
    }
}
