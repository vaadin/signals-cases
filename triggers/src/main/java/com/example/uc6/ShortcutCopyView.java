package com.example.uc6;

import com.example.ShortcutTrigger;
import com.example.views.MainLayout;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.trigger.internal.Action;
import com.vaadin.flow.component.trigger.internal.PropertyInput;
import com.vaadin.flow.component.trigger.internal.WriteToClipboardAction;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC6 — Copy via keyboard shortcut (Ctrl/Cmd-C).
 * <p>
 * Same clipboard chain as UC1, but the trigger is a {@link ShortcutTrigger}
 * scoped to this view's root element. While the user has focus inside the
 * view, pressing Ctrl+C (or Cmd+C on macOS — Flow maps both to
 * {@link KeyModifier#CONTROL}) reads the field's current value via
 * {@link PropertyInput} and copies it.
 * <p>
 * {@code ShortcutTrigger} is a local shim in {@code com.example} — see its
 * Javadoc.
 */
@Route(value = "uc6", layout = MainLayout.class)
@PageTitle("UC6 — Shortcut copy")
@Menu(order = 6, title = "UC6 — Shortcut copy")
@StyleSheet("uc6.css")
public class ShortcutCopyView extends VerticalLayout {

    public ShortcutCopyView() {
        addClassName("uc6-view");
        add(new H1("UC6 — Copy via keyboard shortcut"));
        add(new Paragraph(
                "Press Ctrl+C (or Cmd+C) while focus is anywhere in this view. "
                        + "A ShortcutTrigger scoped to the view fires the same "
                        + "clipboard chain UC1 uses — without needing a button."));

        TextField field = new TextField("Source text");
        field.setId("source");
        field.setValue("Copied by Ctrl+C");
        field.addClassName("source-field");

        Action.Input<String> value = new PropertyInput<>(field, "value",
                String.class);
        new ShortcutTrigger(this, Key.KEY_C, KeyModifier.CONTROL)
                .triggers(new WriteToClipboardAction(value, null));

        add(field);
    }
}
