package com.example.uc2;

import java.util.List;

import com.example.views.MainLayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.FullscreenState;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * UC2 — Slideshow / presentation mode.
 * <p>
 * A small carousel of slides that can be projected with
 * {@link com.vaadin.flow.component.page.Page#requestFullscreen()}. Page-level
 * fullscreen (rather than {@link
 * com.vaadin.flow.component.Component#requestFullscreen()}) is the right choice
 * here: it keeps the full document tree mounted, so application shortcuts —
 * Left/Right for navigation — keep working without any wrapper-aware glue.
 */
@Route(value = "uc2", layout = MainLayout.class)
@Menu(order = 2, title = "UC2 — Slideshow")
public class SlideshowView extends VerticalLayout {

    private static final List<String> SLIDES = List.of(
            "Welcome to Fullscreen 101",
            "The page enters fullscreen via Page#requestFullscreen()",
            "Keyboard shortcuts still reach the server",
            "fullscreenSignal() reactively reports the current state",
            "Press Escape to return to the editor");

    private final Span slideContent = new Span(SLIDES.get(0));
    private final Span counter = new Span();
    private final Span stateBadge = new Span();
    private final Div stage = new Div();

    private int index;

    public SlideshowView() {
        add(new H1("UC2 — Slideshow / presentation mode"));
        add(new Paragraph(
                "Click “Present” to project this deck fullscreen. "
                        + "Use ← / → to navigate — the "
                        + "shortcuts reach the server even while the page is "
                        + "fullscreen because Page#requestFullscreen() does "
                        + "not unmount any DOM."));

        stateBadge.addClassName("status-badge");
        add(stateBadge);

        stage.addClassName("slideshow");
        stage.add(slideContent);
        add(stage);

        Div footer = new Div();
        footer.addClassName("slideshow-footer");
        footer.add(counter);
        add(footer);

        Button prev = new Button("Previous", e -> show(index - 1));
        prev.addClickShortcut(Key.ARROW_LEFT);
        Button next = new Button("Next", e -> show(index + 1));
        next.addClickShortcut(Key.ARROW_RIGHT);
        Button present = new Button("Present",
                e -> getUI().ifPresent(ui -> ui.getPage().requestFullscreen()));
        present.addThemeVariants(ButtonVariant.PRIMARY);
        Button exit = new Button("Exit fullscreen",
                e -> getUI().ifPresent(ui -> ui.getPage().exitFullscreen()));
        add(new HorizontalLayout(prev, next, present, exit));

        show(0);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        Signal<FullscreenState> fs = attachEvent.getUI().getPage()
                .fullscreenSignal();

        stateBadge.bindText(fs.map(SlideshowView::badgeText));
        stateBadge.bindClassName("fullscreen",
                fs.map(s -> s == FullscreenState.FULLSCREEN));
        stateBadge.bindClassName("unsupported",
                fs.map(s -> s == FullscreenState.UNSUPPORTED));
        stage.bindClassName("live",
                fs.map(s -> s == FullscreenState.FULLSCREEN));
    }

    private void show(int target) {
        index = Math.floorMod(target, SLIDES.size());
        slideContent.setText(SLIDES.get(index));
        counter.setText("Slide " + (index + 1) + " of " + SLIDES.size());
    }

    private static String badgeText(FullscreenState state) {
        return switch (state) {
        case FULLSCREEN -> "Presenting — Escape to return";
        case NOT_FULLSCREEN -> "Press Present to start the slideshow";
        case UNSUPPORTED -> "Fullscreen is not supported in this browser";
        case UNKNOWN -> "Detecting fullscreen support…";
        };
    }
}
