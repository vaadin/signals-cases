package com.example.uc1;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.example.scheduling.SchedulerService;
import com.example.views.MainLayout;
import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.PageVisibility;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * UC1 — Update when active.
 * <p>
 * A live server clock updates every second while the tab is visible. The effect
 * of {@link com.vaadin.flow.component.page.Page#pageVisibilitySignal()} is
 * reactive: as soon as the user hides the tab the periodic task is cancelled,
 * so the websocket stays quiet. On return, ticking resumes.
 */
@Route(value = "uc1", layout = MainLayout.class)
@Menu(order = 1, title = "UC1 — Update when active")
public class UpdateWhenActiveView extends VerticalLayout {

    private static final DateTimeFormatter TIME = DateTimeFormatter
            .ofPattern("HH:mm:ss");

    private final SchedulerService scheduler;

    private final Span timeLabel = new Span("--:--:--");
    private final Span counterLabel = new Span("0");
    private final Span statusBadge = new Span();

    private @Nullable ScheduledFuture<?> tickTask;
    private int updates;

    public UpdateWhenActiveView(SchedulerService scheduler) {
        this.scheduler = scheduler;

        add(new H1("UC1 — Update when active"));
        add(new Paragraph("The card below updates the server clock every "
                + "second only while the tab is visible. Hide the tab "
                + "for a few seconds, then return — the counter has not "
                + "advanced and no traffic was sent in the meantime."));

        timeLabel.addClassName("uc1-time");
        counterLabel.addClassName("uc1-counter");
        statusBadge.addClassName("status-badge");

        HorizontalLayout clockRow = new HorizontalLayout(
                new Span("Server time:"), timeLabel);
        clockRow.setAlignItems(Alignment.BASELINE);
        HorizontalLayout counterRow = new HorizontalLayout(
                new Span("Updates received:"), counterLabel);
        counterRow.setAlignItems(Alignment.BASELINE);

        add(clockRow, counterRow, new H2("Status"), statusBadge);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();

        Signal.effect(this, () -> {
            PageVisibility state = ui.getPage().pageVisibilitySignal().get();
            applyState(state);
            if (state == PageVisibility.VISIBLE) {
                startTicking(ui);
            } else {
                stopTicking();
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        stopTicking();
        super.onDetach(detachEvent);
    }

    private void startTicking(UI ui) {
        if (tickTask != null && !tickTask.isCancelled()) {
            return;
        }
        tickTask = scheduler.scheduleAtFixedRate(ui, () -> {
            updates++;
            timeLabel.setText(LocalTime.now().format(TIME));
            counterLabel.setText(Integer.toString(updates));
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void stopTicking() {
        if (tickTask != null) {
            tickTask.cancel(false);
            tickTask = null;
        }
    }

    private void applyState(PageVisibility state) {
        statusBadge.getElement().getClassList()
                .removeAll(List.of("paused", "hidden"));
        switch (state) {
        case VISIBLE -> statusBadge.setText("Updating…");
        case VISIBLE_NOT_FOCUSED -> {
            statusBadge.setText("Paused — window not focused");
            statusBadge.addClassName("paused");
        }
        case HIDDEN -> {
            statusBadge.setText("Paused — tab hidden");
            statusBadge.addClassName("hidden");
        }
        case UNKNOWN -> {
            statusBadge.setText("Visibility unknown");
            statusBadge.addClassName("paused");
        }
        }
    }
}
