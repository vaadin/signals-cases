package com.example.uc7;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.clipboard.Clipboard;
import com.vaadin.flow.component.clipboard.ClipboardAvailability;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * UC7 — Detect availability and degrade gracefully.
 * <p>
 * The PRD requires a way to detect whether clipboard read/write is available
 * (e.g. HTTPS context, no restrictive iframe, no denied permission) so the
 * application can hide or disable controls proactively rather than letting
 * a copy attempt silently fail.
 * <p>
 * {@link Clipboard#availabilityHintSignal()} returns a {@link Signal} of
 * {@link ClipboardAvailability} that starts as {@link
 * ClipboardAvailability#UNKNOWN} and resolves to {@code AVAILABLE} or
 * {@code UNSUPPORTED} once the client probes the API. The button and status
 * banner update reactively via {@link Signal#effect}.
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

        Span status = new Span();
        status.getStyle().set("padding", "var(--aura-space-s)")
                .set("border-radius", "var(--aura-border-radius-m)")
                .set("display", "block");

        Button copyButton = new Button("Copy");
        Clipboard.copyOnClick(copyButton, "https://example.com/share/abc123",
                () -> Notification.show("Copied"),
                () -> Notification.show("Copy failed"));

        Signal<ClipboardAvailability> availability = Clipboard
                .availabilityHintSignal();
        Signal.effect(this, () -> {
            switch (availability.get()) {
                case AVAILABLE -> {
                    status.setText("Clipboard is available.");
                    status.getStyle().set("background",
                            "var(--aura-success-color-10pct)");
                    copyButton.setEnabled(true);
                }
                case UNSUPPORTED -> {
                    status.setText("Clipboard is unavailable in this context "
                            + "— the copy button is disabled.");
                    status.getStyle().set("background",
                            "var(--aura-error-color-10pct)");
                    copyButton.setEnabled(false);
                }
                case UNKNOWN -> {
                    status.setText("Checking clipboard availability…");
                    status.getStyle().set("background",
                            "var(--aura-contrast-5pct)");
                    copyButton.setEnabled(false);
                }
            }
        });

        add(status, copyButton);
    }
}
