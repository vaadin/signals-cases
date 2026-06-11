package com.example.uc15;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.trigger.internal.ClickTrigger;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC15 — Scroll a target into view on click.
 * <p>
 * Two "Jump to …" buttons each wired to a {@link ScrollIntoViewAction}
 * pointing at a section far down the page. The simplest possible custom
 * action that doesn't take inputs or report outcome — just calls a method
 * on the captured target.
 */
@Route(value = "uc15", layout = MainLayout.class)
@PageTitle("UC15 — Scroll into view")
@Menu(order = 15, title = "UC15 — Scroll into view")
@StyleSheet("uc15.css")
public class ScrollIntoViewView extends VerticalLayout {

    public ScrollIntoViewView() {
        addClassName("uc15-view");
        add(new H1("UC15 — Scroll into view"));
        add(new Paragraph(
                "Click a button to smoothly scroll the corresponding section "
                        + "into the centre of the viewport. The custom "
                        + "ScrollIntoViewAction is 5 effective lines — capture "
                        + "the target, call scrollIntoView."));

        Div sectionA = new Div("Section A");
        sectionA.setId("section-a");
        sectionA.addClassName("section");

        Div sectionB = new Div("Section B");
        sectionB.setId("section-b");
        sectionB.addClassName("section");

        Div sectionC = new Div("Section C");
        sectionC.setId("section-c");
        sectionC.addClassName("section");

        Button toA = new Button("Jump to A");
        toA.setId("to-a");
        new ClickTrigger(toA).triggers(new ScrollIntoViewAction(sectionA));

        Button toB = new Button("Jump to B");
        toB.setId("to-b");
        new ClickTrigger(toB).triggers(new ScrollIntoViewAction(sectionB));

        Button toC = new Button("Jump to C");
        toC.setId("to-c");
        new ClickTrigger(toC).triggers(new ScrollIntoViewAction(sectionC));

        add(new HorizontalLayout(toA, toB, toC));
        add(new Div(sectionA, sectionB, sectionC));
    }
}
