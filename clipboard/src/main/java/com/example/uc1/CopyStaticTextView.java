package com.example.uc1;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.clipboard.Clipboard;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC1 — Copy static text on click.
 * <p>
 * The most common case. The text is known when the view is rendered, so the
 * write happens entirely on the client inside the click handler — no server
 * round-trip, works in all browsers. Success and failure callbacks are
 * delivered to the server so the application can show feedback.
 */
@Route(value = "uc1", layout = MainLayout.class)
@PageTitle("UC1 — Copy static text on click")
@Menu(order = 1, title = "UC1 — Copy static text")
public class CopyStaticTextView extends VerticalLayout {

    public CopyStaticTextView() {
        add(new H1("UC1 — Copy static text on click"));
        add(new Paragraph(
                "The text is known when the view is rendered. The copy happens "
                        + "client-side in the click handler so it works in all browsers."));

        String link = "https://example.com/share/abc123";
        add(new Paragraph("Text to copy:"));
        add(new Pre(link));

        Button copyButton = new Button("Copy link");
        Clipboard.copyOnClick(copyButton, link,
                () -> Notification.show("Link copied"),
                () -> Notification.show("Copy failed"));

        add(copyButton);
    }
}
