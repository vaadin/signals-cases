package com.example.uc10;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.trigger.internal.ClickTrigger;
import com.vaadin.flow.component.trigger.internal.LiteralInput;
import com.vaadin.flow.component.trigger.internal.WriteToClipboardAction;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * UC10 — Client-side copy followed by a server-side callback.
 * <p>
 * The clipboard write still happens client-side inside the user gesture (so
 * the browser allows it), but {@link WriteToClipboardAction} can also take an
 * {@code onCopied} consumer that fires on the server right after the
 * browser's promise resolves — useful for logging, analytics, or updating
 * server-side state. Here the callback increments a counter signal,
 * observable in the badge next to the button.
 */
@Route(value = "uc10", layout = MainLayout.class)
@PageTitle("UC10 — Copy + server callback")
@Menu(order = 10, title = "UC10 — Server callback")
@StyleSheet("uc10.css")
public class CopyAndCountView extends VerticalLayout {

    private static final String INVITATION = "https://example.com/invite/abc";

    public CopyAndCountView() {
        addClassName("uc10-view");
        add(new H1("UC10 — Copy + server callback"));
        add(new Paragraph(
                "The Copy button copies a static invitation link to the "
                        + "clipboard. WriteToClipboardAction's onCopied consumer "
                        + "fires on the UI thread after the browser confirms the "
                        + "write — here it bumps the counter so the app can record "
                        + "how many times the link was copied."));

        ValueSignal<Integer> counter = new ValueSignal<>(0);

        Button copy = new Button("Copy invitation link");
        copy.setId("copy");

        Span count = new Span();
        count.setId("count");
        count.addClassName("count-badge");
        count.bindText(counter.map(i -> "copied " + i + "×"));

        new ClickTrigger(copy).triggers(new WriteToClipboardAction(
                new LiteralInput<>(INVITATION), null,
                copied -> counter.set(counter.peek() + 1),
                err -> {
                    /* swallowed — see UC's javadoc */ }));

        add(new HorizontalLayout(copy, count));
    }
}
