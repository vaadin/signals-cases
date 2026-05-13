package com.example.uc1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.FullscreenTestSupport;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.page.FullscreenState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = ImageLightboxView.class)
class ImageLightboxViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithHeadingAndThumbnails() {
        navigate(ImageLightboxView.class);

        assertTrue($view(H1.class).all().stream()
                .anyMatch(h -> "UC1 — Image lightbox".equals(h.getText())));

        long thumbCount = $view(Div.class).all().stream()
                .filter(d -> d.getClassNames().contains("lightbox-thumb"))
                .count();
        assertEquals(6, thumbCount, "expected one thumb per photo");
    }

    @Test
    void clickingThumbnailSwitchesSelection() {
        navigate(ImageLightboxView.class);
        runPendingSignalsTasks();

        Div ocean = $view(Div.class).all().stream()
                .filter(d -> d.getClassNames().contains("lightbox-thumb"))
                .filter(d -> "Ocean".equals(d.getText())).findFirst()
                .orElseThrow();
        test(ocean).click();
        runPendingSignalsTasks();

        assertTrue($view(Span.class).all().stream()
                .anyMatch(s -> "Ocean".equals(s.getText())),
                "selected name should update on thumbnail click");
    }

    @Test
    void stateBadgeReflectsFullscreenSignal() {
        navigate(ImageLightboxView.class);
        runPendingSignalsTasks();

        assertBadgeContains("Detecting");

        FullscreenTestSupport.setFullscreenState(FullscreenState.NOT_FULLSCREEN);
        runPendingSignalsTasks();
        assertBadgeContains("Click a thumbnail");

        FullscreenTestSupport.setFullscreenState(FullscreenState.FULLSCREEN);
        runPendingSignalsTasks();
        assertBadgeContains("Fullscreen");

        FullscreenTestSupport.setFullscreenState(FullscreenState.UNSUPPORTED);
        runPendingSignalsTasks();
        assertBadgeContains("not supported");
    }

    private void assertBadgeContains(String fragment) {
        assertTrue(
                $view(Span.class).all().stream()
                        .anyMatch(s -> s.getClassNames().contains("status-badge")
                                && s.getText() != null
                                && s.getText().contains(fragment)),
                "expected status badge to contain \"" + fragment + "\"");
    }
}
