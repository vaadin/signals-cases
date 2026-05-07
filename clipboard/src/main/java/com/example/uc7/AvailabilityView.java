package com.example.uc7;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Clipboard;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC7 — Detect availability and degrade gracefully.
 * <p>
 * The PRD requires a way to detect whether clipboard read/write is available
 * (e.g. HTTPS context, no restrictive iframe, no denied permission) so the
 * application can hide or disable controls proactively rather than letting
 * a copy attempt silently fail.
 * <p>
 * The {@code 25.2.clipboard-SNAPSHOT} build does not yet expose an
 * availability API. {@link #isClipboardAvailable()} is a stub that always
 * reports {@code true} so the rest of this view compiles; replace its body
 * with the real call once the method lands.
 */
@Route(value = "uc7", layout = MainLayout.class)
@PageTitle("UC7 — Detect availability")
@Menu(order = 7, title = "UC7 — Availability")
public class AvailabilityView extends VerticalLayout {

    public AvailabilityView() {
        add(new H1("UC7 — Detect availability and degrade gracefully"));
        add(new Paragraph(
                "Clipboard access is unavailable on plain HTTP, in restrictive "
                        + "iframes, and when the user has denied permission. "
                        + "Query availability up front and disable controls "
                        + "instead of letting a copy fail silently."));

        boolean available = isClipboardAvailable();

        Span status = new Span(
                available ? "Clipboard is available."
                        : "Clipboard is unavailable in this context — "
                                + "the copy button is disabled.");
        status.getStyle().set("padding", "var(--aura-space-s)")
                .set("border-radius", "var(--aura-border-radius-m)")
                .set("background",
                        available ? "var(--aura-success-color-10pct)"
                                : "var(--aura-error-color-10pct)")
                .set("display", "block");

        Button copyButton = new Button("Copy");
        copyButton.setEnabled(available);
        if (available) {
            Clipboard.copyOnClick(copyButton, "https://example.com/share/abc123",
                    () -> Notification.show("Copied"),
                    () -> Notification.show("Copy failed"));
        }

        add(status, copyButton);
    }

    private static boolean isClipboardAvailable() {
        // Stub: Clipboard.isAvailable() is not yet in the snapshot API.
        return true;
    }
}
