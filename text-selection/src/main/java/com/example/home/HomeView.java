package com.example.home;

import com.example.views.MainLayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Text Selection API Use Cases")
@Menu(order = 0, title = "Home")
public class HomeView extends VerticalLayout {

    public HomeView() {
        add(new H1("Text Selection API — use cases"));
        add(new Paragraph(
                "Each link below exercises one use case of the Text Selection "
                        + "API on TextField and TextArea. The API gives the "
                        + "server programmatic control over selection, cursor "
                        + "position, and clipboard, and exposes the current "
                        + "selection as a Signal so it can drive reactive UI."));

        UnorderedList list = new UnorderedList();
        MenuConfiguration.getMenuEntries().stream()
                .filter(entry -> !entry.path().isEmpty())
                .forEach(entry -> list.add(item(entry)));
        add(list);
    }

    private ListItem item(MenuEntry entry) {
        ListItem li = new ListItem();
        li.add(new Div(link(entry)));
        return li;
    }

    private static Component link(MenuEntry entry) {
        if (entry.menuClass() != null) {
            return new RouterLink(entry.title(), entry.menuClass());
        }
        return new Anchor(entry.path(), entry.title());
    }
}
