package com.example.home;

import com.example.common.BaseHomeView;
import com.example.views.MainLayout;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Text Selection API Use Cases")
@Menu(order = 0, title = "Home")
public class HomeView extends BaseHomeView {

    public HomeView() {
        super("Text Selection API — use cases",
                "Each link below exercises one use case of the Text Selection "
                        + "API on TextField and TextArea. The API gives the "
                        + "server programmatic control over selection, cursor "
                        + "position, and clipboard, and exposes the current "
                        + "selection as a Signal so it can drive reactive UI.");
        addMenuLinkList();
    }
}
