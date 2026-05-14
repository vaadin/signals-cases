package com.example.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;

/**
 * Shared scaffolding for the per-module home views. The constructor adds an H1
 * headline and an intro paragraph; subclasses choose how to render the body of
 * the page:
 * <ul>
 * <li>{@link #addMenuLinkList()} — append a simple bulleted list of links
 * auto-generated from {@link MenuConfiguration#getMenuEntries()}, suitable for
 * modules that haven't hand-curated their home page.</li>
 * <li>{@link #homeCard(String, String, String, Class)} — build a single feature
 * card with a tag, title, description and "Open →" CTA, for modules that
 * hand-curate their home page.</li>
 * </ul>
 */
public abstract class BaseHomeView extends VerticalLayout {

    protected BaseHomeView(String headline, String intro) {
        add(new H1(headline));
        add(new Paragraph(intro));
    }

    protected void addMenuLinkList() {
        UnorderedList list = new UnorderedList();
        MenuConfiguration.getMenuEntries().stream()
                .filter(entry -> !entry.path().isEmpty()).forEach(entry -> {
                    ListItem li = new ListItem();
                    li.add(new Div(menuLink(entry)));
                    list.add(li);
                });
        add(list);
    }

    private static Component menuLink(MenuEntry entry) {
        if (entry.menuClass() != null) {
            return new RouterLink(entry.title(), entry.menuClass());
        }
        return new Anchor(entry.path(), entry.title());
    }

    protected static Card homeCard(String tag, String title, String description,
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
