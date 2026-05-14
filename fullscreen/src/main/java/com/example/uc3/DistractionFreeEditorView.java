package com.example.uc3;

import com.example.views.MainLayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.FullscreenState;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * UC3 — Distraction-free editor.
 * <p>
 * A pane that wraps the text area together with a Done button and live word
 * counter is enlarged via
 * {@link com.vaadin.flow.component.Component#requestFullscreen()}. The rest of
 * the view (heading, intro, navigation drawer) is hidden by the wrapper, so the
 * writer sees only the editor, the word counter, and the Done button. Exit with
 * Escape or by clicking Done — which calls
 * {@link com.vaadin.flow.component.Component#exitFullscreen()} on the pane
 * directly, with no need to thread the UI through to
 * {@code Page.exitFullscreen()}.
 */
@Route(value = "uc3", layout = MainLayout.class)
@Menu(order = 3, title = "UC3 — Distraction-free editor")
public class DistractionFreeEditorView extends VerticalLayout {

    private final TextArea editor = new TextArea();
    private final Span wordCount = new Span("0 words");
    private final Span stateBadge = new Span();
    private final Div editorPane = new Div();

    public DistractionFreeEditorView() {
        add(new H1("UC3 — Distraction-free editor"));
        add(new Paragraph(
                "Click “Expand to fullscreen” to focus only on the text. The "
                        + "rest of the app — navigation, header, this "
                        + "paragraph — is hidden by the fullscreen wrapper. "
                        + "The Done button and word counter live inside the "
                        + "same pane as the editor, so they stay visible while "
                        + "fullscreen and Done can exit fullscreen on click."));

        stateBadge.addClassName("status-badge");
        add(stateBadge);

        editor.addClassName("editor-area");
        editor.setLabel("Write something");
        editor.setPlaceholder("Start typing…");
        editor.setValueChangeMode(ValueChangeMode.LAZY);
        editor.addValueChangeListener(e -> updateWordCount(e.getValue()));

        Button expand = new Button("Expand to fullscreen",
                e -> editorPane.requestFullscreen());
        Button done = new Button("Done", e -> editorPane.exitFullscreen());
        wordCount.addClassName("editor-stats");

        HorizontalLayout paneFooter = new HorizontalLayout(done, wordCount);
        paneFooter.addClassName("editor-pane-footer");
        paneFooter.setAlignItems(Alignment.CENTER);
        paneFooter.setWidthFull();

        editorPane.addClassName("editor-pane");
        editorPane.add(editor, paneFooter);

        HorizontalLayout toolbar = new HorizontalLayout(expand);
        toolbar.addClassName("editor-toolbar");
        toolbar.setAlignItems(Alignment.CENTER);
        add(toolbar);

        add(editorPane);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        Signal<FullscreenState> fs = attachEvent.getUI().getPage()
                .fullscreenSignal();

        Signal<Boolean> fullscreen = fs
                .map(s -> s == FullscreenState.FULLSCREEN);
        stateBadge.bindText(fs.map(DistractionFreeEditorView::badgeText));
        stateBadge.bindClassName("fullscreen", fullscreen);
        stateBadge.bindClassName("unsupported",
                fs.map(s -> s == FullscreenState.UNSUPPORTED));
        editorPane.bindClassName("fullscreen", fullscreen);
        editor.bindClassName("fullscreen", fullscreen);
    }

    private void updateWordCount(String value) {
        long words = value == null || value.isBlank() ? 0
                : value.trim().split("\\s+").length;
        wordCount.setText(words + (words == 1 ? " word" : " words"));
    }

    private static String badgeText(FullscreenState state) {
        return switch (state) {
        case FULLSCREEN -> "Focused — Escape to return";
        case NOT_FULLSCREEN -> "Click Expand to write distraction-free";
        case UNSUPPORTED -> "Fullscreen is not supported in this browser";
        case UNKNOWN -> "Detecting fullscreen support…";
        };
    }
}
