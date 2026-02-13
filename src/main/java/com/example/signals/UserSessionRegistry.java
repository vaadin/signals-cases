package com.example.signals;

import org.springframework.stereotype.Component;

import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.shared.SharedListSignal;

/**
 * Application-scoped registry of currently logged-in users. Provides a reactive
 * signal containing the list of active users.
 */
@Component
public class UserSessionRegistry {

    private final SharedListSignal<UserInfo> activeUsersSignal = new SharedListSignal<>(
            UserInfo.class);

    // Computed signal for display names with session numbers and nicknames
    private final Signal<java.util.List<String>> displayNamesSignal = Signal
            .computed(() -> {
                var userSignals = activeUsersSignal.get();

                // Count sessions per username for auto-naming
                java.util.Map<String, Integer> sessionCounts = new java.util.HashMap<>();
                for (var userSignal : userSignals) {
                    String username = userSignal.get().username();
                    sessionCounts.merge(username, 1, Integer::sum);
                }

                // Generate display names
                java.util.Map<String, Integer> currentSessionNumber = new java.util.HashMap<>();
                java.util.List<String> displayNames = new java.util.ArrayList<>();

                for (var userSignal : userSignals) {
                    UserInfo user = userSignal.get();

                    // Check for nickname first
                    if (user.nickname() != null && !user.nickname().isEmpty()) {
                        displayNames.add(user.nickname());
                        continue;
                    }

                    // Fall back to auto-generated name
                    String username = user.username();
                    int sessionNum = currentSessionNumber.merge(username, 1,
                            (a, b) -> a + 1);

                    String displayName;
                    if (sessionCounts.get(username) > 1) {
                        displayName = username + " #" + sessionNum;
                    } else {
                        displayName = username;
                    }
                    displayNames.add(displayName);
                }

                return displayNames;
            });

    /**
     * Get the signal containing the list of active users.
     */
    public SharedListSignal<UserInfo> getActiveUsersSignal() {
        return activeUsersSignal;
    }

    /**
     * Get the computed signal containing formatted display names with session
     * numbers.
     */
    public Signal<java.util.List<String>> getDisplayNamesSignal() {
        return displayNamesSignal;
    }

    /**
     * Register a user as active with a specific session ID.
     */
    public void registerUser(String username, String sessionId) {
        registerUser(username, sessionId, null);
    }

    /**
     * Register a user as active with a specific session ID and initial route.
     */
    public void registerUser(String username, String sessionId,
            String initialRoute) {
        if (username == null || sessionId == null) {
            throw new IllegalArgumentException(
                    "Username and sessionId cannot be null");
        }

        // Check using composite key
        String compositeKey = username + ":" + sessionId;
        boolean exists = activeUsersSignal.get().stream()
                .anyMatch(userSignal -> compositeKey
                        .equals(userSignal.get().getCompositeKey()));

        if (!exists) {
            activeUsersSignal.insertLast(
                    new UserInfo(username, sessionId, initialRoute, null));
        } else if (initialRoute != null) {
            // User already registered, just update the route
            updateUserView(username, sessionId, initialRoute);
        }
    }

    /**
     * Unregister a user (e.g., on logout or session timeout).
     */
    public void unregisterUser(String username, String sessionId) {
        if (username == null || sessionId == null) {
            throw new IllegalArgumentException(
                    "Username and sessionId cannot be null");
        }

        String compositeKey = username + ":" + sessionId;
        activeUsersSignal.get().stream()
                .filter(userSignal -> compositeKey
                        .equals(userSignal.get().getCompositeKey()))
                .findFirst().ifPresent(activeUsersSignal::remove);
    }

    /**
     * Get count of active users.
     */
    public int getActiveUserCount() {
        return activeUsersSignal.get().size();
    }

    /**
     * Check if a user is currently active (any session).
     */
    public boolean isUserActive(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        return activeUsersSignal.get().stream().anyMatch(
                userSignal -> username.equals(userSignal.get().username()));
    }

    /**
     * Check if a specific session is currently active.
     */
    public boolean isSessionActive(String username, String sessionId) {
        if (username == null || sessionId == null) {
            throw new IllegalArgumentException(
                    "Username and sessionId cannot be null");
        }

        String compositeKey = username + ":" + sessionId;
        return activeUsersSignal.get().stream()
                .anyMatch(userSignal -> compositeKey
                        .equals(userSignal.get().getCompositeKey()));
    }

    /**
     * Set a custom nickname for a user session.
     */
    public void setNickname(String username, String sessionId,
            String nickname) {
        String compositeKey = username + ":" + sessionId;
        String trimmedNickname = (nickname == null || nickname.trim().isEmpty())
                ? null
                : nickname.trim();

        activeUsersSignal.get().stream()
                .filter(userSignal -> compositeKey
                        .equals(userSignal.get().getCompositeKey()))
                .findFirst().ifPresent(userSignal -> {
                    UserInfo oldInfo = userSignal.get();
                    userSignal.set(oldInfo.withNickname(trimmedNickname));
                });
    }

    /**
     * Get the nickname for a user session, or null if not set.
     */
    public String getNickname(String username, String sessionId) {
        String compositeKey = username + ":" + sessionId;
        return activeUsersSignal.get().stream()
                .filter(userSignal -> compositeKey
                        .equals(userSignal.get().getCompositeKey()))
                .findFirst().map(userSignal -> userSignal.get().nickname())
                .orElse(null);
    }

    /**
     * Update the current view for a user session.
     */
    public void updateUserView(String username, String sessionId,
            String viewRoute) {
        String compositeKey = username + ":" + sessionId;

        // Find the user and update their view
        activeUsersSignal.get().stream()
                .filter(userSignal -> compositeKey
                        .equals(userSignal.get().getCompositeKey()))
                .findFirst().ifPresent(userSignal -> {
                    UserInfo oldInfo = userSignal.get();
                    userSignal.set(oldInfo.withCurrentView(viewRoute));
                });
    }

    /**
     * Get a reactive signal of display names for users on a specific view.
     */
    public Signal<java.util.List<String>> getActiveUsersOnView(
            String viewRoute) {
        return Signal.computed(() -> {
            var allUsers = activeUsersSignal.get();
            var allDisplayNames = displayNamesSignal.get();

            java.util.List<String> result = new java.util.ArrayList<>();
            for (int i = 0; i < allUsers.size(); i++) {
                UserInfo user = allUsers.get(i).get();
                if (viewRoute.equals(user.currentView())) {
                    result.add(allDisplayNames.get(i));
                }
            }
            return result;
        });
    }

    /**
     * Update the tab activity state for a user session.
     *
     * @param username
     *            the username
     * @param sessionId
     *            the session ID
     * @param isActive
     *            true if the tab is visible/active, false otherwise
     */
    public void updateTabActivity(String username, String sessionId,
            boolean isActive) {
        String compositeKey = username + ":" + sessionId;

        activeUsersSignal.get().stream()
                .filter(userSignal -> compositeKey
                        .equals(userSignal.get().getCompositeKey()))
                .findFirst().ifPresent(userSignal -> {
                    UserInfo oldInfo = userSignal.get();
                    userSignal.set(oldInfo.withTabActive(isActive));
                });
    }

    /**
     * Update the last interaction time for a user session to the current time.
     *
     * @param username
     *            the username
     * @param sessionId
     *            the session ID
     */
    public void updateLastInteraction(String username, String sessionId) {
        String compositeKey = username + ":" + sessionId;

        activeUsersSignal.get().stream()
                .filter(userSignal -> compositeKey
                        .equals(userSignal.get().getCompositeKey()))
                .findFirst().ifPresent(userSignal -> {
                    UserInfo oldInfo = userSignal.get();
                    userSignal.set(oldInfo.withLastInteractionTime(
                            System.currentTimeMillis()));
                });
    }

}
