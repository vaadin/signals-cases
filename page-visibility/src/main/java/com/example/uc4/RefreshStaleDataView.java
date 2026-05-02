package com.example.uc4;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.example.views.MainLayout;
import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.PageVisibility;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * UC4 — Refresh stale data when the user returns.
 * <p>
 * The "USD/EUR" rate card refreshes automatically when the tab regains
 * visibility after at least 5 seconds of being hidden. Quick alt-tabs do not
 * trigger a fetch — useful to avoid flicker when the user is just glancing
 * away. A manual refresh button is always available.
 */
@Route(value = "uc4", layout = MainLayout.class)
@Menu(order = 4, title = "UC4 — Refresh stale data")
public class RefreshStaleDataView extends VerticalLayout {

    private static final long REFRESH_THRESHOLD_SECONDS = 5;

    private final Random rng = new Random();
    private double rate = 1.0823;
    private Instant updatedAt = Instant.now();
    private @Nullable Instant hiddenAt;
    private PageVisibility prevState = PageVisibility.VISIBLE;

    private final Span valueLabel = new Span();
    private final Span timestampLabel = new Span();
    private final Div card = new Div();

    private final ScheduledExecutorService scheduler = Executors
            .newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "uc4-tick");
                t.setDaemon(true);
                return t;
            });
    private @Nullable ScheduledFuture<?> tickTask;
    private @Nullable ScheduledFuture<?> highlightTask;

    public RefreshStaleDataView() {
        add(new H1("UC4 — Refresh stale data on return"));
        add(new Paragraph("The card below shows a fake USD/EUR exchange "
                + "rate. Switch away for more than 5 seconds and back — "
                + "the rate auto-refreshes and the card highlights. "
                + "Quick glances away (under 5 s) are ignored."));

        valueLabel.addClassName("rate-value");
        Span pair = new Span("USD/EUR ");
        pair.getStyle().set("font-size", "0.9rem").set("color",
                "var(--lumo-secondary-text-color, #666)");

        Div pairRow = new Div(pair);
        Div valueRow = new Div(valueLabel);
        Div timestampRow = new Div(timestampLabel);
        timestampRow.getStyle().set("font-size", "0.85rem")
                .set("color", "var(--lumo-secondary-text-color, #666)")
                .set("margin-top", "0.5rem");

        card.addClassName("rate-card");
        card.add(pairRow, valueRow, timestampRow);

        Button manual = new Button("Refresh now", e -> refresh());
        add(card, manual);

        renderRate();
        renderTimestamp();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();

        Signal.effect(this, () -> {
            PageVisibility state = ui.getPage().pageVisibilitySignal().get();
            handleTransition(prevState, state);
            prevState = state;
            if (state == PageVisibility.VISIBLE) {
                startTimestampTick(ui);
            } else {
                stopTimestampTick();
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        stopTimestampTick();
        if (highlightTask != null) {
            highlightTask.cancel(false);
        }
        scheduler.shutdownNow();
        super.onDetach(detachEvent);
    }

    private void handleTransition(PageVisibility from, PageVisibility to) {
        if (from != PageVisibility.HIDDEN && to == PageVisibility.HIDDEN) {
            hiddenAt = Instant.now();
        } else if (from == PageVisibility.HIDDEN
                && to == PageVisibility.VISIBLE) {
            Instant since = hiddenAt;
            hiddenAt = null;
            if (since != null && Duration.between(since, Instant.now())
                    .getSeconds() >= REFRESH_THRESHOLD_SECONDS) {
                refresh();
            }
        }
    }

    private void refresh() {
        rate = Math.max(0.5,
                Math.min(2.0, rate + (rng.nextDouble() - 0.5) * 0.01));
        updatedAt = Instant.now();
        renderRate();
        renderTimestamp();
        triggerHighlight();
    }

    private void renderRate() {
        valueLabel.setText("%.4f".formatted(rate));
    }

    private void renderTimestamp() {
        long secondsAgo = Duration.between(updatedAt, Instant.now())
                .getSeconds();
        timestampLabel.setText("Last updated " + secondsAgo + " s ago");
    }

    private void triggerHighlight() {
        card.addClassName("highlight");
        if (highlightTask != null) {
            highlightTask.cancel(false);
        }
        UI ui = UI.getCurrent();
        highlightTask = scheduler.schedule(
                () -> ui.access(() -> card.removeClassName("highlight")), 600,
                TimeUnit.MILLISECONDS);
    }

    private void startTimestampTick(UI ui) {
        if (tickTask != null && !tickTask.isCancelled()) {
            return;
        }
        tickTask = scheduler.scheduleAtFixedRate(
                () -> ui.access(this::renderTimestamp), 1, 1, TimeUnit.SECONDS);
    }

    private void stopTimestampTick() {
        if (tickTask != null) {
            tickTask.cancel(false);
            tickTask = null;
        }
    }
}
