package com.example.home;

import com.example.uc1.AdaptiveLayoutView;
import com.example.uc2.OrientationViewerView;
import com.example.uc3.RotatePromptView;
import com.example.uc4.LockForVideoView;
import com.example.uc5.LockErrorView;
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
        add(new H1("Screen Orientation API — use cases"));
        add(new Paragraph(
                "Each card below exercises one use case of Page#screenOrientationSignal() "
                        + "and Page#lockOrientation(...). The signal carries the current "
                        + "ScreenOrientation type (portrait-primary, landscape-primary, ...) "
                        + "and rotation angle, and notifies the server whenever the browser "
                        + "reports a change. Lock requests are subject to platform support and "
                        + "usually require fullscreen — see UC4 and UC5."));

        Div cards = new Div();
        cards.addClassName("home-cards");
        cards.add(card("UC1", "Adaptive layout",
                "Side-by-side in landscape, stacked in portrait.",
                AdaptiveLayoutView.class));
        cards.add(card("UC2", "Orientation viewer",
                "Live type, angle and support state, with a rotating arrow.",
                OrientationViewerView.class));
        cards.add(card("UC3", "Rotate-your-device overlay",
                "Block the view until the user rotates to the required orientation.",
                RotatePromptView.class));
        cards.add(card("UC4", "Lock landscape for video",
                "Enter fullscreen + lock landscape while playing media.",
                LockForVideoView.class));
        cards.add(card("UC5", "Lock error UX",
                "Display SecurityError / NotSupportedError / AbortError reactively.",
                LockErrorView.class));
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
