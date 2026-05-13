package com.example.uc5;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.example.views.MainLayout;
import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.FullscreenSession;
import com.vaadin.flow.component.page.FullscreenSessionState;
import com.vaadin.flow.component.page.FullscreenState;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * UC5 — Kiosk: detect unexpected exit.
 * <p>
 * Kiosk-style screens want to stay fullscreen until the operator decides
 * otherwise — the operator pressing Escape is a problem; our own
 * {@code exit()} call is normal. The Fullscreen API surfaces this distinction
 * through {@link FullscreenSession#stateSignal()}: when the session terminates
 * the state lands on {@link FullscreenSessionState#EXITED_BY_USER
 * EXITED_BY_USER} (Escape, browser gesture) or
 * {@link FullscreenSessionState#EXITED_BY_CODE EXITED_BY_CODE} (server-side
 * {@code exit()}). This view reacts directly to those values — no
 * "expectingExit" flag, no transition watcher.
 */
@Route(value = "uc5", layout = MainLayout.class)
@Menu(order = 5, title = "UC5 — Kiosk: detect exit")
public class KioskExitDetectionView extends VerticalLayout {

    private static final DateTimeFormatter TIME = DateTimeFormatter
            .ofPattern("HH:mm:ss");

    private final Span stateBadge = new Span();
    private final Span unexpectedWarning = new Span();
    private final Div stage = new Div();
    private final Div log = new Div();

    private @Nullable FullscreenSession session;

    public KioskExitDetectionView() {
        add(new H1("UC5 — Kiosk: detect unexpected exit"));
        add(new Paragraph(
                "Click “Enter kiosk”. Each session has its own lifecycle "
                        + "signal — pressing Escape transitions it to "
                        + "EXITED_BY_USER, clicking “Exit kiosk” transitions "
                        + "it to EXITED_BY_CODE. The log shows the terminal "
                        + "state directly; no transition tracking required."));

        stateBadge.addClassName("status-badge");
        unexpectedWarning.addClassName("unexpected-warning");

        stage.addClassName("kiosk-stage");
        stage.add(new Span("Kiosk content"));

        Button enter = new Button("Enter kiosk", e -> getUI().ifPresent(ui -> {
            unexpectedWarning.setText("");
            FullscreenSession s = ui.getPage().requestFullscreen();
            session = s;
            bindSession(s);
        }));
        enter.addThemeVariants(ButtonVariant.PRIMARY);

        Button exit = new Button("Exit kiosk", e -> {
            FullscreenSession s = session;
            if (s != null) {
                s.exit();
            }
        });

        Button clear = new Button("Clear log", e -> log.removeAll());

        log.addClassName("kiosk-log");

        add(new HorizontalLayout(enter, exit, clear, stateBadge,
                unexpectedWarning));
        add(stage);
        add(new Paragraph("Activity log:"));
        add(log);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        Signal<FullscreenState> fs = ui.getPage().fullscreenSignal();
        stateBadge.bindText(fs.map(KioskExitDetectionView::badgeText));
        stateBadge.bindClassName("fullscreen",
                fs.map(s -> s == FullscreenState.FULLSCREEN));
        stateBadge.bindClassName("unsupported",
                fs.map(s -> s == FullscreenState.UNSUPPORTED));
        stage.bindClassName("live",
                fs.map(s -> s == FullscreenState.FULLSCREEN));
    }

    private void bindSession(FullscreenSession s) {
        Signal.effect(this, () -> {
            FullscreenSessionState state = s.stateSignal().get();
            switch (state) {
            case ACTIVE -> appendLog("Entered fullscreen", null);
            case EXITED_BY_USER -> {
                appendLog("Exit (UNEXPECTED — user pressed Escape)",
                        "unexpected");
                unexpectedWarning.setText("Kiosk exited unexpectedly!");
            }
            case EXITED_BY_CODE -> appendLog("Exit (expected)", null);
            case REJECTED -> appendLog("Request REJECTED: "
                    + s.error().orElse("no error message"), "unexpected");
            case PENDING -> {
                // initial state; nothing to log yet
            }
            }
        });
    }

    private void appendLog(String message, @Nullable String cls) {
        Div line = new Div();
        line.setText("[" + LocalTime.now().format(TIME) + "] " + message);
        if (cls != null) {
            line.addClassName(cls);
        }
        log.addComponentAsFirst(line);
    }

    private static String badgeText(FullscreenState state) {
        return switch (state) {
        case FULLSCREEN -> "Kiosk running";
        case NOT_FULLSCREEN -> "Kiosk stopped";
        case UNSUPPORTED -> "Kiosk not supported";
        case UNKNOWN -> "Detecting…";
        };
    }
}
