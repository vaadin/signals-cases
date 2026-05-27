package com.example.home;

import com.example.common.BaseHomeView;
import com.example.uc1.CatalogView;
import com.example.uc2.OrdersView;
import com.example.uc3.UsersView;
import com.example.uc4.ProjectsView;
import com.example.uc5.SettingsView;
import com.example.uc6.DashboardView;
import com.example.uc7.SitemapView;
import com.example.views.MainLayout;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@Menu(order = 0, title = "Home")
public class HomeView extends BaseHomeView {

    public HomeView() {
        super("Route Hierarchy — use cases",
                "Each card below builds a breadcrumb trail as a HorizontalLayout "
                        + "of RouterLinks, driven by the Flow-core route-hierarchy "
                        + "API from PR #24451: RouteHierarchy.resolveAncestors(...) "
                        + "/ resolveParent(...) walk a view's ancestors via the "
                        + "@RouteParent annotation first and URL-prefix matching as "
                        + "the fallback. The Breadcrumbs component itself is not part "
                        + "of that PR — these views show what you can build directly "
                        + "on top of the walker.");

        Div cards = new Div();
        cards.addClassName("home-cards");
        cards.add(homeCard("UC1", "URL-prefix trail",
                "Nested routes whose URLs nest — ancestors found with zero "
                        + "annotations.",
                CatalogView.class));
        cards.add(homeCard("UC2", "@RouteParent override",
                "A detail page whose URL is not a prefix of its conceptual "
                        + "parent.",
                OrdersView.class));
        cards.add(homeCard("UC3", "Dynamic leaf label",
                "The current crumb shows a runtime title via HasDynamicTitle.",
                UsersView.class));
        cards.add(homeCard("UC4", "Parameter-preserving links",
                "Ancestor links carry the :projectId through the whole trail.",
                ProjectsView.class));
        cards.add(homeCard("UC5", "Up-one-level button",
                "A single \"↑ Up\" link built from resolveParent(...).",
                SettingsView.class));
        cards.add(homeCard("UC6", "Layout-wide auto breadcrumbs",
                "One breadcrumb bar in a parent layout, rebuilt on every "
                        + "navigation.",
                DashboardView.class));
        cards.add(homeCard("UC7", "Route-tree sitemap",
                "resolveAncestors used as a graph-builder to render the whole "
                        + "hierarchy.",
                SitemapView.class));
        add(cards);
    }
}
