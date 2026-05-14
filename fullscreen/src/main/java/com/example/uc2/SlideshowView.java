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
 * <p>
 * The browser fullscreens the entire document, but the view chrome (heading,
 * intro paragraph, footer, button bar) is hidden while presenting via
 * {@link com.vaadin.flow.component.HasElement#bindClassName} so the audience
 * only sees the slide.
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
    private final H1 heading = new H1("UC2 — Slideshow / presentation mode");
    private final Paragraph intro = new Paragraph(
            "Click “Present” to project this deck fullscreen. "
                    + "Page#requestFullscreen() puts the whole document in "
                    + "fullscreen — Left/Right shortcuts still reach the "
                    + "server because no DOM is unmounted. The view's own "
                    + "chrome is hidden while presenting so the audience "
                    + "sees just the slide.");
    private final Div footer = new Div();
    private final HorizontalLayout buttons = new HorizontalLayout();

    private int index;

    public SlideshowView() {
        add(heading);
        add(intro);

        stateBadge.addClassName("status-badge");
        add(stateBadge);

        stage.addClassName("slideshow");
        stage.add(slideContent);
        add(stage);

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
        buttons.add(prev, next, present, exit);
        add(buttons);

        show(0);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        Signal<FullscreenState> fs = attachEvent.getUI().getPage()
                .fullscreenSignal();

        Signal<Boolean> live = fs
                .map(s -> s == FullscreenState.FULLSCREEN);

        stateBadge.bindText(fs.map(SlideshowView::badgeText));
        stateBadge.bindClassName("fullscreen", live);
        stateBadge.bindClassName("unsupported",
                fs.map(s -> s == FullscreenState.UNSUPPORTED));
        stage.bindClassName("live", live);

        // Hide everything except the slide while presenting, so Page-level
        // fullscreen looks like a slideshow rather than a fullscreened app.
        heading.bindClassName("hidden-while-presenting", live);
        intro.bindClassName("hidden-while-presenting", live);
        stateBadge.bindClassName("hidden-while-presenting", live);
        footer.bindClassName("hidden-while-presenting", live);
        buttons.bindClassName("hidden-while-presenting", live);
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
