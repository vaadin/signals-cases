package com.example.muc03;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import com.example.signals.UserSessionRegistry;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.html.Span;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = MUC03View.class)
@WithMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MUC03NicknameUpdateTest extends SpringBrowserlessTest {

    @Autowired
    private MUC03Signals muc03Signals;

    @Autowired
    private UserSessionRegistry userSessionRegistry;

    @Test
    void leaderboardUpdatesWhenNicknameChanges() {
        navigate(MUC03View.class);
        runPendingSignalsTasks();

        // Register a second user and give them a score
        userSessionRegistry.registerUser("userB", "sessionB", "muc-03");
        muc03Signals.initializePlayerScore("userB", "sessionB");
        muc03Signals.startNewRound(100, 100);
        muc03Signals.awardPoint("userB", "sessionB");
        runPendingSignalsTasks();

        // Verify userB's name appears in the leaderboard
        assertTrue($view(Span.class).all().stream().anyMatch(
                        s -> s.getText() != null
                                && s.getText().contains("userB")
                                && s.getText().contains("1 points")),
                "Leaderboard should show userB with 1 point");

        // Now change userB's nickname
        userSessionRegistry.setNickname("userB", "sessionB", "CoolPlayer");
        runPendingSignalsTasks();

        // The leaderboard should now show the nickname instead
        assertTrue($view(Span.class).all().stream().anyMatch(
                        s -> s.getText() != null
                                && s.getText().contains("CoolPlayer")
                                && s.getText().contains("1 points")),
                "Leaderboard should update to show nickname 'CoolPlayer'");

        // The old username should no longer appear in the score label
        assertTrue($view(Span.class).all().stream().noneMatch(
                        s -> s.getText() != null
                                && s.getText().contains("userB")
                                && s.getText().contains("1 points")),
                "Leaderboard should no longer show 'userB' in score label");
    }

    @Test
    void leaderboardUpdatesWhenNicknameCleared() {
        navigate(MUC03View.class);
        runPendingSignalsTasks();

        // Register userB, give them a nickname and a score
        userSessionRegistry.registerUser("userB", "sessionB", "muc-03");
        userSessionRegistry.setNickname("userB", "sessionB", "CoolPlayer");
        muc03Signals.initializePlayerScore("userB", "sessionB");
        muc03Signals.startNewRound(100, 100);
        muc03Signals.awardPoint("userB", "sessionB");
        runPendingSignalsTasks();

        assertTrue($view(Span.class).all().stream().anyMatch(
                        s -> s.getText() != null
                                && s.getText().contains("CoolPlayer")),
                "Leaderboard should show nickname");

        // Clear the nickname
        userSessionRegistry.setNickname("userB", "sessionB", "");
        runPendingSignalsTasks();

        // Should revert to username
        assertTrue($view(Span.class).all().stream().anyMatch(
                        s -> s.getText() != null
                                && s.getText().contains("userB")
                                && s.getText().contains("1 points")),
                "Leaderboard should revert to username when nickname cleared");
    }
}
