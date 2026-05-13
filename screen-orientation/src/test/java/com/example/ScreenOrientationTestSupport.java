package com.example;

import java.lang.reflect.Method;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.ScreenOrientation;

/**
 * Shared helper for browserless tests that need to drive the screen
 * orientation signal on {@link Page#screenOrientationSignal()}. The setter on
 * Page is package-private (only the JS bridge calls it in production), so
 * tests reach through reflection. See {@code API-GAPS.md} — Flow has no
 * {@code ScreenOrientationSimulator} yet.
 */
public final class ScreenOrientationTestSupport {

    private ScreenOrientationTestSupport() {
    }

    public static void setScreenOrientation(ScreenOrientation orientation,
            int angle) {
        try {
            Page page = UI.getCurrent().getPage();
            Method setter = Page.class.getDeclaredMethod("setScreenOrientation",
                    String.class, String.class);
            setter.setAccessible(true);
            setter.invoke(page, orientation.getClientValue(),
                    Integer.toString(angle));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to invoke Page#setScreenOrientation reflectively",
                    e);
        }
    }
}
