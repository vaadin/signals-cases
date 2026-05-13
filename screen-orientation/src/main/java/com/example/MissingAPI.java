package com.example;

import com.vaadin.flow.component.Component;

/**
 * Temporary helpers for behaviour the screen-orientation use cases need but
 * Flow does not yet expose. See {@code API-GAPS.md} in this module.
 */
public final class MissingAPI {

    private MissingAPI() {
    }

    /**
     * Requests the browser to enter fullscreen mode for the given component's
     * element. Flow has no first-class fullscreen API, but every desktop and
     * mobile browser exposes {@code element.requestFullscreen()} on the
     * Element interface. Locking screen orientation is in practice only
     * honoured inside a fullscreen document, so UC4 needs this shim.
     * <p>
     * Tracked at vaadin/flow — no upstream issue yet. When a real
     * {@code Component.requestFullscreen()} (or {@code Page.requestFullscreen})
     * lands, drop this method and the {@code executeJs} call.
     *
     * @param component
     *            the component whose element should enter fullscreen
     */
    public static void requestFullscreen(Component component) {
        // The .catch is a courtesy — fullscreen requests reject if not
        // invoked from a user gesture or already in fullscreen, and we just
        // want the lock attempt to proceed rather than turn into a console
        // error.
        component.getElement().executeJs(
                "this.requestFullscreen?.().catch(() => {})");
    }

    /**
     * Asks the browser to exit fullscreen mode. Uses {@code document} because
     * fullscreen is a document-level concept; the component is only used to
     * resolve the right window.
     */
    public static void exitFullscreen(Component component) {
        component.getElement().executeJs(
                "if (document.fullscreenElement) { document.exitFullscreen?.().catch(() => {}); }");
    }
}
