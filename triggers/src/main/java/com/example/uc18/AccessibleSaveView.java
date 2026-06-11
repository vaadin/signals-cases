package com.example.uc18;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.trigger.internal.ClickTrigger;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC18 — Accessibility announcement on save.
 * <p>
 * Clicking "Save" updates the visible badge and also fires an
 * {@link AnnounceAction} that writes into an aria-live region. Screen-reader
 * users hear "Saved" without focus moving from the button. The aria-live
 * region is visually hidden via the {@code sr-only} class.
 */
@Route(value = "uc18", layout = MainLayout.class)
@PageTitle("UC18 — Accessibility announce")
@Menu(order = 18, title = "UC18 — Accessibility announce")
@StyleSheet("uc18.css")
public class AccessibleSaveView extends VerticalLayout {

    public AccessibleSaveView() {
        addClassName("uc18-view");
        add(new H1("UC18 — Accessibility announce"));
        add(new Paragraph(
                "Click Save. Sighted users see the badge update; screen-reader "
                        + "users hear \"Saved at …\" from a visually hidden "
                        + "aria-live region — the AnnounceAction writes the "
                        + "message into the region's textContent."));

        Div liveRegion = new Div();
        liveRegion.setId("live");
        liveRegion.addClassName("sr-only");
        liveRegion.getElement().setAttribute("aria-live", "polite");
        liveRegion.getElement().setAttribute("aria-atomic", "true");

        Span visibleBadge = new Span("(not saved yet)");
        visibleBadge.setId("badge");
        visibleBadge.addClassName("badge");

        Button save = new Button("Save");
        save.setId("save");
        save.addClickListener(e -> visibleBadge.setText("Saved at "
                + java.time.LocalTime.now().withNano(0)));

        new ClickTrigger(save).triggers(
                new AnnounceAction(liveRegion, "Saved"));

        add(new HorizontalLayout(save, visibleBadge));
        add(liveRegion);
    }
}
