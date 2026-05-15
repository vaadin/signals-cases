package com.example.home;

import com.example.common.BaseHomeView;
import com.example.uc1.CopyFieldValueView;
import com.example.uc2.CopyCodeSnippetView;
import com.example.uc3.CopySelectValueView;
import com.example.uc4.ShareUrlView;
import com.example.uc5.CustomActionView;
import com.example.views.MainLayout;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@Menu(order = 0, title = "Home")
public class HomeView extends BaseHomeView {

    public HomeView() {
        super("Trigger / Action API — use cases",
                "Each card below exercises one use case of the new "
                        + "com.vaadin.flow.component.trigger API. A Trigger fires on "
                        + "the client (e.g. a click), reads zero or more Outputs from "
                        + "the DOM, and runs one or more Actions — all inside the "
                        + "original user-gesture handler, so browser APIs gated on a "
                        + "gesture (clipboard, fullscreen, share) work without a "
                        + "server round-trip.");

        Div cards = new Div();
        cards.addClassName("home-cards");
        cards.add(homeCard("UC1", "Copy field value",
                "Click a button, copy the current value of a text field.",
                CopyFieldValueView.class));
        cards.add(homeCard("UC2", "Copy code snippet",
                "Copy a <pre> block's textContent — Output works on any element.",
                CopyCodeSnippetView.class));
        cards.add(homeCard("UC3", "Copy from a select",
                "Read the currently-selected option's value via PropertyOutput.",
                CopySelectValueView.class));
        cards.add(homeCard("UC4", "Share-URL widget",
                "Server-generated URL, copied without a server round-trip.",
                ShareUrlView.class));
        cards.add(homeCard("UC5", "Custom action",
                "Register a custom action on the client via @JsModule.",
                CustomActionView.class));
        add(cards);
    }
}
