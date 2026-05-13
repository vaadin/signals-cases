package com.example;

import java.lang.reflect.Method;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.WakeLock;

/**
 * Shared helper for browserless tests that need to drive
 * {@link WakeLock#activeSignal()} transitions. The setter on {@code WakeLock}
 * is package-private (only the JS bridge calls it in production), so tests
 * reach through reflection. This shim should disappear if upstream exposes a
 * {@code WakeLockSimulator} similar to {@code GeolocationSimulator}.
 */
public final class WakeLockTestSupport {

    private WakeLockTestSupport() {
    }

    public static void simulateState(String state) {
        try {
            WakeLock wakeLock = UI.getCurrent().getPage().getWakeLock();
            Method setter = WakeLock.class.getDeclaredMethod("setActive",
                    String.class);
            setter.setAccessible(true);
            setter.invoke(wakeLock, state);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to invoke WakeLock#setActive reflectively", e);
        }
    }

    public static void simulateAcquired() {
        simulateState("ACTIVE");
    }

    public static void simulateReleased() {
        simulateState("RELEASED");
    }
}
