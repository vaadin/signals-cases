package com.example.home;

import com.example.uc1.ShareThisPageView;
import com.example.uc2.CopyLinkFallbackView;
import com.example.uc3.CustomMessageView;
import com.example.uc4.ShareListItemsView;
import com.example.uc5.ShareFeedbackView;
import com.example.uc6.ShareInviteLinkView;
import com.example.views.MainLayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route(value = "", layout = MainLayout.class)
@Menu(order = 0, title = "Home")
public class HomeView extends VerticalLayout {

    public HomeView() {
        add(new H1("Web Share API — use cases"));
        add(new Paragraph(
                "Each card below exercises one use case of Page#share(...) and "
                        + "Page#shareSupportSignal(). The signal reports "
                        + "SUPPORTED, UNSUPPORTED, or UNKNOWN, and share() "
                        + "invokes the browser's native share sheet on "
                        + "platforms that expose navigator.share (most "
                        + "mobile browsers and recent desktop Safari/Edge)."));

        Div cards = new Div();
        cards.addClassName("home-cards");
        cards.add(card("UC1", "Share this page",
                "Hand the current URL + title to the native share sheet.",
                ShareThisPageView.class));
        cards.add(card("UC2", "Copy-link fallback",
                "Swap between native Share and Copy-link based on the signal.",
                CopyLinkFallbackView.class));
        cards.add(card("UC3", "Share a custom message",
                "Fill title/text/url, preview the payload, share.",
                CustomMessageView.class));
        cards.add(card("UC4", "Share each item in a list",
                "Per-row share icon on a list of articles.",
                ShareListItemsView.class));
        cards.add(card("UC5", "Share with completion feedback",
                "React to success, cancellation, and errors.",
                ShareFeedbackView.class));
        cards.add(card("UC6", "Share an invite link",
                "Generate a one-shot join URL and hand it to the sheet.",
                ShareInviteLinkView.class));
        add(cards);
    }

    private Card card(String tag, String title, String description,
            Class<? extends Component> target) {
        Card card = new Card();
        card.addThemeVariants(CardVariant.OUTLINED);
        card.addClassName("home-card");
        Div tagLabel = new Div(tag);
        tagLabel.addClassName("home-card-tag");
        card.setHeader(tagLabel);
        card.setTitle(new Div(title));
        card.add(new Paragraph(description));
        card.addToFooter(new RouterLink("Open →", target));
        return card;
    }
}
