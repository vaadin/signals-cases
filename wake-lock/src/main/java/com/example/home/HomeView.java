package com.example.home;

import com.example.uc1.ManualToggleView;
import com.example.uc2.RecipeView;
import com.example.uc3.SlideshowView;
import com.example.uc4.WorkoutTimerView;
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
        add(new H1("Screen Wake Lock API — use cases"));
        add(new Paragraph(
                "Each card below exercises one use case of Page#getWakeLock(). "
                        + "WakeLock#request() asks the browser to keep the screen "
                        + "awake; WakeLock#activeSignal() reflects whether the "
                        + "browser is currently holding the lock. The client "
                        + "transparently re-acquires the lock when the tab "
                        + "becomes visible again, so a single request() covers "
                        + "the lifetime of a view."));

        Div cards = new Div();
        cards.addClassName("home-cards");
        cards.add(card("UC1", "Manual keep-awake toggle",
                "Toggle the lock on demand and watch the signal flip.",
                ManualToggleView.class));
        cards.add(card("UC2", "Recipe — lifetime of view",
                "Lock acquired on attach, released on detach.",
                RecipeView.class));
        cards.add(card("UC3", "Presentation slideshow",
                "Lock held only while a presentation is running.",
                SlideshowView.class));
        cards.add(card("UC4", "Workout interval timer",
                "Lock follows an internal running-state signal.",
                WorkoutTimerView.class));
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
