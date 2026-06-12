package com.example.uc3;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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
 * UC3 — Copy a value from a live signal that mutates over time.
 * <p>
 * A server-side click handler on "Tick" increments the counter signal.
 * {@link SignalInput} mirrors the current value into a property on the
 * host element via an effect, so every signal change is reflected on the
 * next click without any round-trip on the gesture itself.
 */
@Route(value = "uc3", layout = MainLayout.class)
@PageTitle("UC3 — Live signal counter")
@Menu(order = 3, title = "UC3 — Live signal")
@StyleSheet("uc3.css")
public class LiveSignalCounterView extends VerticalLayout {

    public LiveSignalCounterView() {
        addClassName("uc3-view");
        add(new H1("UC3 — Copy a live signal value"));
        add(new Paragraph(
                "Press Tick to increment the counter. The Copy button reads "
                        + "the current value via SignalInput — every signal change "
                        + "updates the mirror, so the next click always copies the "
                        + "latest value with no server round-trip."));

        ValueSignal<String> counter = new ValueSignal<>("0");

        Span display = new Span();
        display.setId("counter");
        display.addClassName("counter");
        display.bindText(counter);

        Button tick = new Button("Tick");
        tick.setId("tick");
        tick.addClickListener(e -> counter.set(
                Integer.toString(Integer.parseInt(counter.peek()) + 1)));

        Button copy = new Button("Copy current value");
        copy.setId("copy");

        new ClickTrigger(copy).triggers(new WriteToClipboardAction(
                new SignalInput<>(copy, counter), null));

        add(new HorizontalLayout(tick, display), copy);
    }
}
