package com.example.home;

import com.example.uc1.ImageLightboxView;
import com.example.uc2.SlideshowView;
import com.example.uc3.DistractionFreeEditorView;
import com.example.uc4.ReactiveLayoutView;
import com.example.uc5.KioskExitDetectionView;
import com.example.uc6.ChartExpandView;
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
        add(new H1("Fullscreen API — use cases"));
        add(new Paragraph(
                "Each card below exercises one scenario of the new Fullscreen "
                        + "API. Component#requestFullscreen() wraps a single "
                        + "component (overlays and theming keep working), "
                        + "Page#requestFullscreen() takes the entire document "
                        + "fullscreen, and Page#fullscreenSignal() reactively "
                        + "reports FULLSCREEN, NOT_FULLSCREEN, UNSUPPORTED or "
                        + "UNKNOWN. Browsers require a real user click to "
                        + "enter fullscreen."));

        Div cards = new Div();
        cards.addClassName("home-cards");
        cards.add(card("UC1", "Image lightbox",
                "Click an image to fullscreen it without losing overlays.",
                ImageLightboxView.class));
        cards.add(card("UC2", "Slideshow / presentation",
                "Page-level fullscreen with keyboard navigation.",
                SlideshowView.class));
        cards.add(card("UC3", "Distraction-free editor",
                "Expand a text area to fullscreen for focused writing.",
                DistractionFreeEditorView.class));
        cards.add(card("UC4", "Reactive layout",
                "fullscreenSignal() drives density and visible widgets.",
                ReactiveLayoutView.class));
        cards.add(card("UC5", "Kiosk: detect exit",
                "React when the user presses Escape to leave fullscreen.",
                KioskExitDetectionView.class));
        cards.add(card("UC6", "Chart expand",
                "Per-card expand buttons fullscreen a single chart.",
                ChartExpandView.class));
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
