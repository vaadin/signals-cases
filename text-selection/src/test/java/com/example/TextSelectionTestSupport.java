package com.example;

import java.lang.reflect.Method;

import com.vaadin.flow.component.HasSelection;
import com.vaadin.flow.component.SelectionRange;

/**
 * Shared helper for browserless tests that need to drive the
 * {@code selectionSignal()} of a TextField/TextArea without a real browser.
 * The setter that the JS bridge normally invokes on the server is
 * package-private, so tests reach through reflection — mirrors
 * {@code PageVisibilityTestSupport} in the page-visibility module. Content
 * is derived from the field's current value the same way the client does.
 */
public final class TextSelectionTestSupport {

    private TextSelectionTestSupport() {
    }

    public static void setSelection(HasSelection field, int start, int end) {
        String value = readValue(field);
        int safeStart = Math.max(0, Math.min(start, value.length()));
        int safeEnd = Math.max(safeStart, Math.min(end, value.length()));
        String content = value.substring(safeStart, safeEnd);

        try {
            Method setter = field.getClass().getDeclaredMethod(
                    "setSelectionFromClient", SelectionRange.class);
            setter.setAccessible(true);
            setter.invoke(field,
                    new SelectionRange(safeStart, safeEnd, content));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to invoke setSelectionFromClient reflectively",
                    e);
        }
    }

    private static String readValue(HasSelection field) {
        try {
            Object v = field.getClass().getMethod("getValue").invoke(field);
            return v == null ? "" : v.toString();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "HasSelection field does not expose getValue()", e);
        }
    }
}
