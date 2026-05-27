package com.example.uc6;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

/**
 * UC6 — Layout-wide auto breadcrumbs (leaf, {@code uc6/team/:member}).
 * <p>
 * Implements {@link HasDynamicTitle}; the layout's shared breadcrumb reads the
 * live instance from {@code AfterNavigationEvent#getActiveChain()}, so the
 * dynamic member name shows up in the trail without this view touching the bar.
 */
@Route(value = "uc6/team/:member", layout = TeamLayout.class)
public class MemberView extends VerticalLayout
        implements BeforeEnterObserver, HasDynamicTitle {

    private final H1 heading = new H1();
    private String memberName = "Unknown member";

    public MemberView() {
        add(heading);
        add(new Paragraph("This view never references the breadcrumb. The "
                + "parent layout's AfterNavigationObserver read this instance "
                + "from the active chain and called getPageTitle() for the leaf "
                + "label."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String memberId = event.getRouteParameters().get("member").orElse("?");
        memberName = TeamMembers.nameOf(memberId);
        heading.setText(memberName);
    }

    @Override
    public String getPageTitle() {
        return memberName;
    }
}
