package com.example.uc2;

import com.example.ClickAction;
import com.example.ShortcutTrigger;
import com.example.views.MainLayout;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.trigger.internal.SetPropertyAction;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC2 — Submit on Enter, then disable the submit button.
 * <p>
 * Pressing Enter inside the field fires a {@link ShortcutTrigger} that chains
 * two actions: a {@link ClickAction} that synthesises a click on the submit
 * button (running its server-side handler) and a {@link SetPropertyAction}
 * that disables the button so a stray double-press can't re-submit. The
 * disable runs client-side immediately. The button's click listener also
 * calls {@code setEnabled(false)} server-side so the next render keeps the
 * disabled state.
 * <p>
 * Order matters: click first, then disable — browsers block clicks on
 * already-disabled elements.
 * <p>
 * {@code ShortcutTrigger} and {@code ClickAction} are local shims in
 * {@code com.example}; both are gone from the published API.
 */
@Route(value = "uc2", layout = MainLayout.class)
@PageTitle("UC2 — Submit + disable chain")
@Menu(order = 2, title = "UC2 — Submit + disable")
@StyleSheet("uc2.css")
public class SubmitAndDisableView extends VerticalLayout {

    public SubmitAndDisableView() {
        addClassName("uc2-view");
        add(new H1("UC2 — Submit on Enter, then disable"));
        add(new Paragraph(
                "Type a message and press Enter. A ShortcutTrigger on the "
                        + "field clicks \"Send\" (running its click handler) and "
                        + "immediately disables it client-side, so an accidental "
                        + "second Enter can't re-submit. The click listener also "
                        + "disables the button server-side."));

        TextField field = new TextField("Message");
        field.setId("message");
        field.addClassName("message-field");

        Span echo = new Span();
        echo.setId("echo");
        echo.addClassName("echo");

        Button send = new Button("Send");
        send.setId("send");
        send.addClickListener(e -> {
            echo.setText("Sent: " + field.getValue());
            send.setEnabled(false);
        });

        new ShortcutTrigger(field, Key.ENTER).triggers(new ClickAction(send),
                new SetPropertyAction<>(send, "disabled", true));

        add(new HorizontalLayout(field, send), echo);
    }
}
