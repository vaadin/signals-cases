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
 * The leaf label is the dynamic member name, produced instance-free by
 * {@link MemberTitleGenerator} (flow#24550): the layout's shared breadcrumb is
 * signal-bound and resolves each crumb with {@code getTitle(class, params)}, so
 * the member name appears in the trail without this view touching the bar.
 */
@Route(value = "uc6/team/:member", layout = TeamLayout.class)
@PageTitle(generator = MemberTitleGenerator.class)
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
