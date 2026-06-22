package com.example.uc1;

import com.example.views.MainLayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.WebShareSupport;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * UC1 — Share this page.
 * <p>
 * The most basic Web Share scenario: a single "Share this page" button hands
 * the current page's title and URL to the browser's native share sheet. The
 * button reflects {@link Page#shareSupportSignal()} reactively: while the
 * signal is still {@link WebShareSupport#UNKNOWN} the button stays disabled
 * with a "Detecting…" label; once a real value arrives it either enables (on
 * {@link WebShareSupport#SUPPORTED}) or stays disabled with an explanation (on
 * {@link WebShareSupport#UNSUPPORTED}).
 */
@Route(value = "uc1", layout = MainLayout.class)
@Menu(order = 1, title = "UC1 — Share this page")
public class ShareThisPageView extends VerticalLayout {

    private static final String ARTICLE_TITLE = "Vaadin Flow — Web Share API";
    private static final String ARTICLE_URL = "https://vaadin.com/";

    private final Button shareButton = new Button("Share this page",
            VaadinIcon.SHARE.create());
    private final Span statusBadge = new Span();

    public ShareThisPageView() {
        add(new H1("UC1 — Share this page"));
        add(new Paragraph("Hands the current page's title and URL to the "
                + "browser's native share sheet. Use a mobile browser to "
                + "see the share sheet pop up — on desktop Firefox the "
                + "button stays disabled and explains why."));

        shareButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        statusBadge.addClassName("status-badge");

        HorizontalLayout row = new HorizontalLayout(shareButton, statusBadge);
        row.setAlignItems(Alignment.CENTER);
        add(row);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        Page page = attachEvent.getUI().getPage();
        Signal<WebShareSupport> support = page.shareSupportSignal();

        statusBadge.bindText(support.map(ShareThisPageView::statusText));
        statusBadge.bindClassName("unsupported",
                support.map(s -> s == WebShareSupport.UNSUPPORTED));
        statusBadge.bindClassName("unknown",
                support.map(s -> s == WebShareSupport.UNKNOWN));

        Signal.effect(this, () -> shareButton
                .setEnabled(support.get() == WebShareSupport.SUPPORTED));

        shareButton.addClickListener(e -> {
            page.share(ARTICLE_TITLE, null, ARTICLE_URL);
            // Fallback notification for desktop developers — the native
            // sheet only shows on mobile; on a desktop browser that does
            // expose navigator.share (Edge, Safari) the user still sees
            // something happen.
            Notification.show(
                    "Share invoked: \"" + ARTICLE_TITLE + "\" → " + ARTICLE_URL,
                    2500, Notification.Position.BOTTOM_START);
        });
    }

    private static String statusText(WebShareSupport state) {
        return switch (state) {
        case SUPPORTED -> "Native sharing available";
        case UNSUPPORTED -> "Browser does not support navigator.share";
        case UNKNOWN -> "Detecting…";
        };
    }
}
