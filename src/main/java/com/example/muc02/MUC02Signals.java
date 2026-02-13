package com.example.muc02;

import org.springframework.stereotype.Component;

import com.vaadin.flow.signals.WritableSignal;
import com.vaadin.flow.signals.shared.SharedMapSignal;

/**
 * Application-scoped signals for MUC02: Cursor Positions
 */
@Component
public class MUC02Signals {

    public record CursorPosition(int x, int y) {
        // Default constructor for Jackson deserialization
        public CursorPosition() {
            this(0, 0);
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    // MapSignal where key is "username:sessionId" and value is CursorPosition
    private final SharedMapSignal<CursorPosition> sessionCursorsSignal = new SharedMapSignal<>(
            CursorPosition.class);

    public SharedMapSignal<CursorPosition> getSessionCursorsSignal() {
        return sessionCursorsSignal;
    }

    public WritableSignal<CursorPosition> getCursorSignalForUser(
            String username, String sessionId) {
        String sessionKey = username + ":" + sessionId;
        sessionCursorsSignal
                .putIfAbsent(sessionKey, new CursorPosition(0, 0));
        return sessionCursorsSignal.get().get(sessionKey);
    }

    public void unregisterCursor(String username, String sessionId) {
        String sessionKey = username + ":" + sessionId;
        sessionCursorsSignal.remove(sessionKey);
    }
}
