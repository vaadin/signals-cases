package com.example.home;

import com.example.uc1.UpdateWhenActiveView;
import com.example.uc2.PresenceAvatarsView;
import com.example.uc3.NotificationGatingView;
import com.example.uc4.RefreshStaleDataView;
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
        add(new H1("Page Visibility API — use cases"));
        add(new Paragraph(
                "Each card below exercises one use case of Page#pageVisibilitySignal(). "
                        + "The signal reports VISIBLE, VISIBLE_NOT_FOCUSED, HIDDEN or UNKNOWN "
                        + "and reactively notifies the server whenever the user's tab "
                        + "visibility or focus changes."));

        Div cards = new Div();
        cards.addClassName("home-cards");
        cards.add(card("UC1", "Update when active",
                "Pause server work while the tab is hidden.",
                UpdateWhenActiveView.class));
        cards.add(card("UC2", "Presence avatars",
                "\"Away\" status broadcast across browsers.",
                PresenceAvatarsView.class));
        cards.add(card("UC3", "Notification gating",
                "Switch between in-tab toast and Web Push delivery.",
                NotificationGatingView.class));
        cards.add(card("UC4", "Refresh stale data",
                "Re-fetch automatically when the user returns.",
                RefreshStaleDataView.class));
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
