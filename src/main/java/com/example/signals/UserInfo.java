package com.example.signals;

import org.jspecify.annotations.Nullable;

/**
 * Information about an active user session.
 */
public record UserInfo(String username, String sessionId,
        @Nullable String currentView, @Nullable String nickname,
        long sessionStartTime, boolean isTabActive, long lastInteractionTime,
        int colorIndex) {

    /** Number of colors in the palette defined in styles.css */
    public static final int COLOR_COUNT = 30;

    private static final int DEFAULT_COLOR_INDEX = -1;

    // Constructor with current view and nickname (legacy compatibility)
    public UserInfo(String username, String sessionId,
            @Nullable String currentView, @Nullable String nickname) {
        this(username, sessionId, currentView, nickname,
                System.currentTimeMillis(), true, System.currentTimeMillis(),
                DEFAULT_COLOR_INDEX);
    }

    // Constructor without view or nickname (defaults to null)
    public UserInfo(String username, String sessionId) {
        this(username, sessionId, null, null, System.currentTimeMillis(), true,
                System.currentTimeMillis(), DEFAULT_COLOR_INDEX);
    }

    /**
     * Returns the CSS variable reference for this user's color, e.g.
     * {@code var(--vaadin-user-color-3)}.
     */
    public String cssColor() {
        if (colorIndex < 0) {
            return "#9E9E9E";
        }
        return "var(--vaadin-user-color-" + colorIndex + ")";
    }

    // Generate composite key for tracking
    public String getCompositeKey() {
        return username + ":" + sessionId;
    }

    // Helper to create updated instance with new view
    public UserInfo withCurrentView(@Nullable String newView) {
        return new UserInfo(username, sessionId, newView, nickname,
                sessionStartTime, isTabActive, lastInteractionTime, colorIndex);
    }

    // Helper to create updated instance with new nickname
    public UserInfo withNickname(@Nullable String newNickname) {
        return new UserInfo(username, sessionId, currentView, newNickname,
                sessionStartTime, isTabActive, lastInteractionTime, colorIndex);
    }

    // Helper to create updated instance with new tab activity state
    public UserInfo withTabActive(boolean newTabActive) {
        return new UserInfo(username, sessionId, currentView, nickname,
                sessionStartTime, newTabActive, lastInteractionTime,
                colorIndex);
    }

    // Helper to create updated instance with new last interaction time
    public UserInfo withLastInteractionTime(long newLastInteractionTime) {
        return new UserInfo(username, sessionId, currentView, nickname,
                sessionStartTime, isTabActive, newLastInteractionTime,
                colorIndex);
    }
}
