package com.example.home;

import com.example.common.BaseHomeView;
import com.example.views.MainLayout;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Clipboard API Use Cases")
@Menu(order = 0, title = "Home")
public class HomeView extends BaseHomeView {

    public HomeView() {
        super("Clipboard API — use cases",
                "Each link below exercises one use case of the Vaadin Flow Clipboard API.");
        addMenuLinkList();
        Anchor inspector = new Anchor(
                "https://evercoder.github.io/clipboard-inspector/",
                "Clipboard Inspector");
        inspector.setTarget("_blank");
        add(new Paragraph(new Span("Tip: use "), inspector, new Span(
                " to check what the copy use cases actually put on the clipboard.")));
    }
}
