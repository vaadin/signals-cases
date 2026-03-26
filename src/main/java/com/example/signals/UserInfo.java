package com.example.signals;

import org.jspecify.annotations.Nullable;

/**
 * Information about an active user session.
 */
public record UserInfo(String username, String sessionId,
        @Nullable String currentView, @Nullable String nickname,
        long sessionStartTime, boolean isTabActive, long lastInteractionTime,
        String color) {

    private static final String DEFAULT_COLOR = "#9E9E9E";

    // Constructor with current view and nickname (legacy compatibility)
    public UserInfo(String username, String sessionId,
            @Nullable String currentView, @Nullable String nickname) {
        this(username, sessionId, currentView, nickname,
                System.currentTimeMillis(), true, System.currentTimeMillis(),
                DEFAULT_COLOR);
    }

    // Constructor without view or nickname (defaults to null)
    public UserInfo(String username, String sessionId) {
        this(username, sessionId, null, null, System.currentTimeMillis(), true,
                System.currentTimeMillis(), DEFAULT_COLOR);
    }

    // Generate composite key for tracking
    public String getCompositeKey() {
        return username + ":" + sessionId;
    }

    // Helper to create updated instance with new view
    public UserInfo withCurrentView(@Nullable String newView) {
        return new UserInfo(username, sessionId, newView, nickname,
                sessionStartTime, isTabActive, lastInteractionTime, color);
    }

    // Helper to create updated instance with new nickname
    public UserInfo withNickname(@Nullable String newNickname) {
        return new UserInfo(username, sessionId, currentView, newNickname,
                sessionStartTime, isTabActive, lastInteractionTime, color);
    }

    // Helper to create updated instance with new tab activity state
    public UserInfo withTabActive(boolean newTabActive) {
        return new UserInfo(username, sessionId, currentView, nickname,
                sessionStartTime, newTabActive, lastInteractionTime, color);
    }

    // Helper to create updated instance with new last interaction time
    public UserInfo withLastInteractionTime(long newLastInteractionTime) {
        return new UserInfo(username, sessionId, currentView, nickname,
                sessionStartTime, isTabActive, newLastInteractionTime, color);
    }

    // Helper to create updated instance with new color
    public UserInfo withColor(String newColor) {
        return new UserInfo(username, sessionId, currentView, nickname,
                sessionStartTime, isTabActive, lastInteractionTime, newColor);
    }
}
