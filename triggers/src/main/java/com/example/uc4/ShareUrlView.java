package com.example.uc4;

import java.util.UUID;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Code;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.trigger.internal.ClickTrigger;
import com.vaadin.flow.component.trigger.internal.SignalInput;
import com.vaadin.flow.component.trigger.internal.WriteToClipboardAction;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * UC4 — Server-side {@link ValueSignal} read on click, no round-trip.
 * <p>
 * The URL lives in a {@link ValueSignal} on the server; a {@link SignalInput}
 * mirrors the current value into a property on the host element via an effect,
 * and the click handler copies it without a round-trip. If the signal changes
 * server-side, the mirrored property updates automatically so the next click
 * sees the new value — the only context in which the browser permits
 * {@code navigator.clipboard.write}.
 */
@Route(value = "uc4", layout = MainLayout.class)
@PageTitle("UC4 — Share URL")
@Menu(order = 4, title = "UC4 — Share URL widget")
@StyleSheet("uc4.css")
public class ShareUrlView extends VerticalLayout {

    public ShareUrlView() {
        addClassName("uc4-view");
        add(new H1("UC4 — Share URL widget"));
        add(new Paragraph(
                "The URL below is held in a server-side ValueSignal. "
                        + "SignalInput mirrors its current value to the client so "
                        + "the click handler can copy it without a server "
                        + "round-trip — the only context in which the browser "
                        + "permits navigator.clipboard.write."));

        ValueSignal<String> urlSignal = new ValueSignal<>(
                "https://example.com/share/"
                        + UUID.randomUUID().toString().substring(0, 8));

        Code display = new Code();
        display.setId("share-url");
        display.addClassName("share-url-display");
        display.bindText(urlSignal);

        Button copy = new Button("Copy link");
        copy.setId("copy");

        new ClickTrigger(copy).triggers(new WriteToClipboardAction(
                new SignalInput<>(copy, urlSignal), null));

        add(new HorizontalLayout(display, copy));
    }
}
