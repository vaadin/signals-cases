package com.example.uc2;

import com.example.views.MainLayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.WebShareSupport;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * UC2 — Share with copy-link fallback.
 * <p>
 * Showcases the reactive feature-detection pattern: a single slot in the page
 * holds either a "Share" button (when {@link Page#shareSupportSignal()} is
 * {@link WebShareSupport#SUPPORTED}) or a "Copy link" button (when
 * {@link WebShareSupport#UNSUPPORTED}), driven by a {@link Signal#effect signal
 * effect}. The swap happens automatically — no page reload, no manual
 * feature-detection callback.
 */
@Route(value = "uc2", layout = MainLayout.class)
@Menu(order = 2, title = "UC2 — Copy-link fallback")
@StyleSheet("uc2.css")
public class CopyLinkFallbackView extends VerticalLayout {

    private static final String DOC_TITLE = "Web Share API use cases";
    private static final String DOC_URL = "https://vaadin.com/";

    private final Div actionSlot = new Div();
    private final Paragraph hint = new Paragraph();

    public CopyLinkFallbackView() {
        addClassName("uc2-view");
        add(new H1("UC2 — Share with copy-link fallback"));
        add(new Paragraph("Mobile and modern Safari/Edge users get the "
                + "native share sheet. Desktop Firefox and older browsers "
                + "fall back to a clipboard-copy button — same shareable "
                + "URL either way. The swap is driven entirely by the "
                + "shareSupportSignal()."));

        actionSlot.addClassName("action-slot");
        hint.addClassName("hint");

        add(actionSlot, hint);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        Signal<WebShareSupport> support = ui.getPage().shareSupportSignal();

        Signal.effect(this, () -> renderFor(support.get(), ui));
    }

    private void renderFor(WebShareSupport state, UI ui) {
        actionSlot.removeAll();
        switch (state) {
        case SUPPORTED -> {
            Button share = new Button("Share", VaadinIcon.SHARE.create());
            share.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            share.addClickListener(e -> {
                ui.getPage().share(DOC_TITLE, null, DOC_URL);
                Notification.show("Share invoked", 2000,
                        Notification.Position.BOTTOM_START);
            });
            actionSlot.add(share);
            hint.setText("Browser supports navigator.share — tap to open "
                    + "the native sheet.");
        }
        case UNSUPPORTED -> {
            Button copy = new Button("Copy link", VaadinIcon.LINK.create());
            copy.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            copy.addClickListener(e -> copyLinkToClipboard(ui));
            actionSlot.add(copy);
            hint.setText("Browser does not expose navigator.share — "
                    + "falling back to clipboard.");
        }
        case UNKNOWN -> {
            Button detecting = new Button("Detecting…");
            detecting.setEnabled(false);
            actionSlot.add(detecting);
            hint.setText("Waiting for the first bootstrap report from the "
                    + "browser.");
        }
        }
    }

    private void copyLinkToClipboard(UI ui) {
        // Quick clipboard fallback. Wrapped in a try/catch on the JS side
        // so older browsers without navigator.clipboard still see the
        // notification.
        ui.getPage().executeJs(
                "try { await navigator.clipboard.writeText($0); return true; }"
                        + " catch (e) { return false; }",
                DOC_URL);
        Notification.show("Link copied: " + DOC_URL, 2500,
                Notification.Position.BOTTOM_START);
    }
}
