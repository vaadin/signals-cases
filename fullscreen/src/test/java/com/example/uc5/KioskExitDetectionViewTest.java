package com.example.uc5;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.FullscreenTestSupport;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.page.FullscreenState;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = KioskExitDetectionView.class)
class KioskExitDetectionViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithKioskControls() {
        navigate(KioskExitDetectionView.class);

        assertTrue($view(H1.class).all().stream().anyMatch(h -> h.getText()
                .equals("UC5 — Kiosk: detect unexpected exit")));
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Enter kiosk".equals(b.getText())));
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Exit kiosk".equals(b.getText())));
    }

    @Test
    void unexpectedExitLogsWarning() {
        navigate(KioskExitDetectionView.class);
        runPendingSignalsTasks();

        // Click Enter to reset the expectingExit flag, then simulate the
        // browser entering and exiting fullscreen via the user (Escape).
        Button enter = $view(Button.class).all().stream()
                .filter(b -> "Enter kiosk".equals(b.getText())).findFirst()
                .orElseThrow();
        test(enter).click();
        runPendingSignalsTasks();

        FullscreenTestSupport.setFullscreenState(FullscreenState.FULLSCREEN);
        runPendingSignalsTasks();

        // User presses Escape — we never called exitFullscreen() server-side.
        FullscreenTestSupport.setFullscreenState(FullscreenState.NOT_FULLSCREEN);
        runPendingSignalsTasks();

        assertTrue(
                $view(Div.class).all().stream()
                        .anyMatch(d -> d.getText() != null
                                && d.getText().contains("UNEXPECTED")),
                "log should contain an unexpected-exit entry");
        assertTrue(
                $view(Span.class).all().stream()
                        .anyMatch(s -> s.getClassNames()
                                .contains("unexpected-warning")
                                && s.getText() != null
                                && !s.getText().isEmpty()),
                "warning span should be visible after an unexpected exit");
    }

    @Test
    void programmaticExitLogsExpected() {
        navigate(KioskExitDetectionView.class);
        runPendingSignalsTasks();

        Button enter = $view(Button.class).all().stream()
                .filter(b -> "Enter kiosk".equals(b.getText())).findFirst()
                .orElseThrow();
        test(enter).click();
        runPendingSignalsTasks();

        FullscreenTestSupport.setFullscreenState(FullscreenState.FULLSCREEN);
        runPendingSignalsTasks();

        // Click Exit (sets expectingExit=true and calls page.exitFullscreen()).
        Button exit = $view(Button.class).all().stream()
                .filter(b -> "Exit kiosk".equals(b.getText())).findFirst()
                .orElseThrow();
        test(exit).click();
        runPendingSignalsTasks();
        FullscreenTestSupport.setFullscreenState(FullscreenState.NOT_FULLSCREEN);
        runPendingSignalsTasks();

        assertTrue(
                $view(Div.class).all().stream()
                        .anyMatch(d -> d.getText() != null
                                && d.getText().contains("expected")
                                && !d.getText().contains("UNEXPECTED")),
                "log should contain an expected-exit entry after programmatic exit");
    }
}
