package com.example.muc03;

import org.springframework.stereotype.Component;

import com.vaadin.flow.signals.WritableSignal;
import com.vaadin.flow.signals.shared.SharedMapSignal;
import com.vaadin.flow.signals.shared.SharedValueSignal;

/**
 * Application-scoped signals for MUC03: Click Game
 */
@Component
public class MUC03Signals {

    // MapSignal where key is "username:sessionId" and value is score (Integer)
    private final SharedMapSignal<Integer> leaderboardSignal = new SharedMapSignal<>(
            Integer.class);

    private final SharedValueSignal<Boolean> buttonVisibleSignal = new SharedValueSignal<>(
            false);
    private final SharedValueSignal<Integer> buttonLeftSignal = new SharedValueSignal<>(
            0);
    private final SharedValueSignal<Integer> buttonTopSignal = new SharedValueSignal<>(
            0);
    private final SharedValueSignal<Integer> clicksRemainingSignal = new SharedValueSignal<>(
            0);
    private final SharedValueSignal<Integer> roundNumberSignal = new SharedValueSignal<>(
            0);

    public SharedMapSignal<Integer> getLeaderboardSignal() {
        return leaderboardSignal;
    }

    public WritableSignal<Boolean> getButtonVisibleSignal() {
        return buttonVisibleSignal;
    }

    public WritableSignal<Integer> getButtonLeftSignal() {
        return buttonLeftSignal;
    }

    public WritableSignal<Integer> getButtonTopSignal() {
        return buttonTopSignal;
    }

    public WritableSignal<Integer> getClicksRemainingSignal() {
        return clicksRemainingSignal;
    }

    public WritableSignal<Integer> getRoundNumberSignal() {
        return roundNumberSignal;
    }

    public void initializePlayerScore(String username, String sessionId) {
        String sessionKey = username + ":" + sessionId;
        leaderboardSignal.putIfAbsent(sessionKey, 0);
    }

    public synchronized boolean awardPoint(String username, String sessionId) {
        if (!buttonVisibleSignal.get()
                || clicksRemainingSignal.get() <= 0) {
            return false; // Round already finished
        }

        // Award the point
        String sessionKey = username + ":" + sessionId;
        SharedValueSignal<Integer> scoreSignal = leaderboardSignal.get()
                .get(sessionKey);
        if (scoreSignal != null) {
            scoreSignal.set(scoreSignal.get() + 1);
        } else {
            // Initialize if not present
            leaderboardSignal.put(sessionKey, 1);
        }

        // Decrement clicks remaining
        int remaining = clicksRemainingSignal.get() - 1;
        clicksRemainingSignal.set(remaining);

        // Hide button temporarily (will be repositioned by view)
        buttonVisibleSignal.set(false);

        // Return true if more clicks remain in this round
        return remaining > 0;
    }

    public void startNewRound(int left, int top) {
        buttonLeftSignal.set(left);
        buttonTopSignal.set(top);
        clicksRemainingSignal.set(5); // 5 clicks per round
        roundNumberSignal.set(roundNumberSignal.get() + 1);
        buttonVisibleSignal.set(true);
    }

    public void repositionButton(int left, int top) {
        buttonLeftSignal.set(left);
        buttonTopSignal.set(top);
        buttonVisibleSignal.set(true);
    }

    public void resetLeaderboard() {
        leaderboardSignal.clear();
    }

    public void unregisterScore(String username, String sessionId) {
        String sessionKey = username + ":" + sessionId;
        leaderboardSignal.remove(sessionKey);
    }
}
