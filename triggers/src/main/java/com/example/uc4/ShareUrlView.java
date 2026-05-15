package com.example.uc4;

import java.util.UUID;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.trigger.ClickTrigger;
import com.vaadin.flow.component.trigger.ClipboardCopyAction;
import com.vaadin.flow.component.trigger.Output;
import com.vaadin.flow.component.trigger.PropertyOutput;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC4 — Server-generated share URL, copied without a server round-trip.
 * <p>
 * Highlights the main reason the trigger API exists: the clipboard write must
 * happen synchronously inside the click handler or the browser refuses it. The
 * server renders the URL into a field at view-construction time; from that
 * point on the copy is pure client behaviour and works the first time the user
 * clicks.
 */
@Route(value = "uc4", layout = MainLayout.class)
@PageTitle("UC4 — Share URL")
@Menu(order = 4, title = "UC4 — Share URL widget")
public class ShareUrlView extends VerticalLayout {

    public ShareUrlView() {
        add(new H1("UC4 — Share URL widget"));
        add(new Paragraph(
                "The URL below is generated on the server when the view is "
                        + "rendered. Copying it does not require a round-trip — the "
                        + "click handler reads the field's current value and copies "
                        + "it inside the user gesture, which is the only time the "
                        + "browser permits a clipboard write."));

        String shareUrl = "https://example.com/share/"
                + UUID.randomUUID().toString().substring(0, 8);

        TextField field = new TextField();
        field.setId("share-url");
        field.setValue(shareUrl);
        field.setReadOnly(true);
        field.addClassName("share-url-field");

        Button copy = new Button("Copy link");
        copy.setId("copy");

        Output<String> value = new PropertyOutput<>(field, "value",
                String.class);
        new ClickTrigger(copy).triggers(new ClipboardCopyAction(value));

        add(new HorizontalLayout(field, copy));
    }
}
