package com.example.uc1;

import com.example.ShortcutTrigger;
import com.example.views.MainLayout;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.trigger.internal.CallbackAction;
import com.vaadin.flow.component.trigger.internal.PropertyInput;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC1 — Ctrl+S save shortcut.
 * <p>
 * A custom {@link ShortcutTrigger} listens for Ctrl+S (Cmd+S on macOS) on
 * the view's root. Its {@code preventDefault()} suppresses the browser's
 * built-in "Save Page" dialog so the keystroke reaches our handler instead.
 * A {@link CallbackAction} reads the field's current value via
 * {@link PropertyInput} and posts it to the server, which updates the
 * status badge.
 * <p>
 * Ctrl+C is intentionally NOT used here — the browser's clipboard handler
 * fires before keyboard shortcut listeners can preventDefault, so
 * overriding it from JS is unreliable.
 */
@Route(value = "uc1", layout = MainLayout.class)
@PageTitle("UC1 — Ctrl+S save")
@Menu(order = 1, title = "UC1 — Ctrl+S save")
@StyleSheet("uc1.css")
public class ShortcutSaveView extends VerticalLayout {

    public ShortcutSaveView() {
        addClassName("uc1-view");
        add(new H1("UC1 — Ctrl+S save"));
        add(new Paragraph(
                "Edit the message and press Ctrl+S (or Cmd+S). The "
                        + "ShortcutTrigger suppresses the browser's Save Page "
                        + "dialog and posts the field's current value to a "
                        + "server-side callback that updates the status badge."));

        TextField field = new TextField("Message");
        field.setId("message");
        field.setValue("Edit me, then press Ctrl+S");
        field.addClassName("message-field");

        Span status = new Span("(not saved yet)");
        status.setId("status");
        status.addClassName("status-badge");

        new ShortcutTrigger(this, Key.KEY_S, KeyModifier.CONTROL)
                .triggers(new CallbackAction<>(String.class,
                        value -> status.setText("Saved at "
                                + java.time.LocalTime.now().withNano(0)
                                + ": " + value),
                        new PropertyInput<>(field, "value", String.class)));

        add(field, status);
    }
}
