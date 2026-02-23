package com.example.muc02;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import com.vaadin.flow.signals.shared.SharedMapSignal;
import com.vaadin.flow.signals.shared.SharedValueSignal;

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

    public @Nullable SharedValueSignal<CursorPosition> getCursorSignalForUser(
            String username, String sessionId) {
        String sessionKey = username + ":" + sessionId;
        sessionCursorsSignal.putIfAbsent(sessionKey, new CursorPosition(0, 0));
        var cursors = sessionCursorsSignal.get();
        return cursors != null ? cursors.get(sessionKey) : null;
    }

    public void unregisterCursor(String username, String sessionId) {
        String sessionKey = username + ":" + sessionId;
        sessionCursorsSignal.remove(sessionKey);
    }
}
