package com.example.uc5;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.ScreenOrientation;
import com.vaadin.flow.component.page.ScreenOrientationLockError;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC5 — Lock error UX.
 * <p>
 * Demonstrates the three common error paths surfaced through
 * {@link Page#lockOrientation(ScreenOrientation,
 * com.vaadin.flow.function.SerializableRunnable,
 * com.vaadin.flow.function.SerializableConsumer)
 * Page#lockOrientation(...)}: <ul>
 * <li>{@code SecurityError} — locking without fullscreen on most desktops,
 * <li>{@code NotSupportedError} — browsers that don't implement the API,
 * <li>{@code AbortError} — a newer lock supersedes the previous one. </ul>
 * Each click is logged with the resolved {@link ScreenOrientationLockError}
 * (or a "success" line), giving a quick visual reference for what the new
 * callback-based API surfaces.
 */
@Route(value = "uc5", layout = MainLayout.class)
@PageTitle("UC5 — Lock error UX")
@Menu(order = 5, title = "UC5 — Lock error UX")
public class LockErrorView extends VerticalLayout {

    private static final DateTimeFormatter TIME = DateTimeFormatter
            .ofPattern("HH:mm:ss");

    private final Div log = new Div();

    public LockErrorView() {
        add(new H1("UC5 — Lock error UX"));
        add(new Paragraph("Each button triggers a typical lock failure path. "
                + "Clicks log the resolved success/error directly under the "
                + "controls — handy for verifying how your UI should react "
                + "to each DOMException name."));

        Button lockWithoutFullscreen = new Button(
                "Lock without fullscreen (expect SecurityError)",
                e -> attempt(ScreenOrientation.LANDSCAPE_PRIMARY));
        Button rapidLocks = new Button(
                "Two locks in a row (expect AbortError on the first)",
                e -> {
                    attempt(ScreenOrientation.LANDSCAPE_PRIMARY);
                    attempt(ScreenOrientation.PORTRAIT_PRIMARY);
                });
        Button portraitLock = new Button("Lock to portrait",
                e -> attempt(ScreenOrientation.PORTRAIT_PRIMARY));

        HorizontalLayout actions = new HorizontalLayout(lockWithoutFullscreen,
                rapidLocks, portraitLock);
        actions.setSpacing(true);
        actions.getStyle().set("flex-wrap", "wrap");
        add(actions);

        log.addClassName("uc5-error-log");
        addLog("info", "Click a button above to attempt a lock.");
        add(log);
    }

    private void attempt(ScreenOrientation target) {
        getUI().ifPresent(ui -> ui.getPage().lockOrientation(target,
                () -> addLog("ok",
                        "✓ Locked to " + target.getClientValue()),
                error -> addLog("err",
                        "✗ " + target.getClientValue() + " — " + error.name()
                                + ": " + error.message())));
    }

    private void addLog(String cls, String text) {
        Div line = new Div();
        line.addClassName(cls);
        line.setText("[" + LocalTime.now().format(TIME) + "] " + text);
        // Newest at the top to keep recent activity in view without
        // scrolling.
        log.getElement().insertChild(0, line.getElement());
    }
}
