package com.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.FullscreenState;

/**
 * Thin shim over
 * {@link com.vaadin.flow.component.page.Page#simulateFullscreenChange(FullscreenState)}
 * — kept as a separate type so test classes stay decoupled from the
 * package-private wiring should the API move again, and so a single import
 * matches the {@code PageVisibilityTestSupport} idiom used in the
 * {@code page-visibility/} module.
 */
public final class FullscreenTestSupport {

    private FullscreenTestSupport() {
    }

    public static void setFullscreenState(FullscreenState state) {
        UI.getCurrent().getPage().simulateFullscreenChange(state);
    }
}
