package com.example.uc6;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC6 — Layout-wide auto breadcrumbs (leaf, {@code uc6/team/:member}).
 * <p>
 * The leaf label is the static {@link PageTitle} "Member": the layout's shared
 * breadcrumb is signal-bound and resolves each crumb from its route class, so
 * the concrete member name shows in the heading, not the trail. This view does
 * not touch the bar.
 */
@Route(value = "uc6/team/:member", layout = TeamLayout.class)
@PageTitle("Member")
public class MemberView extends VerticalLayout implements BeforeEnterObserver {

    private final H1 heading = new H1();

    public MemberView() {
        add(heading);
        add(new Paragraph("This view never references the breadcrumb. The "
                + "parent layout's Signal.effect reads the current view from "
                + "UI.routerStateSignal() and the bar resolves the leaf label "
                + "from this view's class."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String memberId = event.getRouteParameters().get("member").orElse("?");
        heading.setText(TeamMembers.nameOf(memberId));
    }
}
