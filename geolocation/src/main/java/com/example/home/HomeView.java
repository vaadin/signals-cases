package com.example.home;

import com.example.common.BaseHomeView;
import com.example.views.MainLayout;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Geolocation API Use Cases")
@Menu(order = 0, title = "Home")
public class HomeView extends BaseHomeView {

    public HomeView() {
        super("Geolocation API — use cases",
                "Each link below exercises one use case of the Vaadin Flow Geolocation API.");
        addMenuLinkList();
    }
}
