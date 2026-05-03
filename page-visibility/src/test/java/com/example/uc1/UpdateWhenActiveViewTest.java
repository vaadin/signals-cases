package com.example.uc1;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.PageVisibility;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UpdateWhenActiveView.class)
class UpdateWhenActiveViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithExpectedHeadings() {
        navigate(UpdateWhenActiveView.class);

        assertTrue($view(H1.class).all().stream()
                .anyMatch(h -> "UC1 — Update when active".equals(h.getText())));
        assertTrue($view(H2.class).all().stream()
                .anyMatch(h -> "Status".equals(h.getText())));
    }

    @Test
    void statusBadgeReflectsPageVisibilityChanges() throws Exception {
        navigate(UpdateWhenActiveView.class);
        runPendingSignalsTasks();

        // Mock UI starts at UNKNOWN; the effect renders the matching label.
        assertBadgeContains("Visibility unknown");

        setPageVisibility(PageVisibility.VISIBLE);
        assertBadgeContains("Updating");

        setPageVisibility(PageVisibility.HIDDEN);
        assertBadgeContains("tab hidden");

        setPageVisibility(PageVisibility.VISIBLE_NOT_FOCUSED);
        assertBadgeContains("not focused");
    }

    private void assertBadgeContains(String fragment) {
        assertTrue(
                $view(Span.class).all().stream()
                        .anyMatch(s -> s.getText() != null
                                && s.getText().contains(fragment)),
                "expected status badge to contain \"" + fragment + "\"");
    }

    /**
     * The Page-side setter is package-private (only the JS bridge calls it).
     * Reflection lets the test simulate browser-driven transitions without
     * standing up a real DOM.
     */
    private void setPageVisibility(PageVisibility state) throws Exception {
        Page page = UI.getCurrent().getPage();
        Method setter = Page.class.getDeclaredMethod("setPageVisibility",
                String.class);
        setter.setAccessible(true);
        setter.invoke(page, state.name());
        runPendingSignalsTasks();
    }
}
