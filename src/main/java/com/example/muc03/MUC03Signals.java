package com.example.muc03;

import org.springframework.stereotype.Component;

import com.vaadin.signals.shared.SharedMapSignal;
import com.vaadin.signals.shared.SharedValueSignal;
import com.vaadin.signals.WritableSignal;

/**
 * Application-scoped signals for MUC03: Click Game
 */
@Component
public class MUC03Signals {

    // MapSignal where key is "username:sessionId" and value is score (Integer)
    private final SharedMapSignal<Integer> leaderboardSignal = new SharedMapSignal<>(
            Integer.class);

    private final WritableSignal<Boolean> buttonVisibleSignal = new SharedValueSignal<>(
            false);
    private final WritableSignal<Integer> buttonLeftSignal = new SharedValueSignal<>(
            0);
    private final WritableSignal<Integer> buttonTopSignal = new SharedValueSignal<>(
            0);
    private final WritableSignal<Integer> clicksRemainingSignal = new SharedValueSignal<>(
            0);
    private final WritableSignal<Integer> roundNumberSignal = new SharedValueSignal<>(
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
        if (!buttonVisibleSignal.value()
                || clicksRemainingSignal.value() <= 0) {
            return false; // Round already finished
        }

        // Award the point
        String sessionKey = username + ":" + sessionId;
        SharedValueSignal<Integer> scoreSignal = leaderboardSignal.value()
                .get(sessionKey);
        if (scoreSignal != null) {
            scoreSignal.value(scoreSignal.value() + 1);
        } else {
            // Initialize if not present
            leaderboardSignal.put(sessionKey, 1);
        }

        // Decrement clicks remaining
        int remaining = clicksRemainingSignal.value() - 1;
        clicksRemainingSignal.value(remaining);

        // Hide button temporarily (will be repositioned by view)
        buttonVisibleSignal.value(false);

        // Return true if more clicks remain in this round
        return remaining > 0;
    }

    public void startNewRound(int left, int top) {
        buttonLeftSignal.value(left);
        buttonTopSignal.value(top);
        clicksRemainingSignal.value(5); // 5 clicks per round
        roundNumberSignal.value(roundNumberSignal.value() + 1);
        buttonVisibleSignal.value(true);
    }

    public void repositionButton(int left, int top) {
        buttonLeftSignal.value(left);
        buttonTopSignal.value(top);
        buttonVisibleSignal.value(true);
    }

    public void resetLeaderboard() {
        leaderboardSignal.clear();
    }

    public void unregisterScore(String username, String sessionId) {
        String sessionKey = username + ":" + sessionId;
        leaderboardSignal.remove(sessionKey);
    }
}
