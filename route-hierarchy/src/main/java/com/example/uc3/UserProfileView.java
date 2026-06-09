package com.example.uc3;

import com.example.views.BreadcrumbBar;
import com.example.views.MainLayout;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

/**
 * UC3 — Dynamic title gap (profile, {@code uc3/:userId}).
 * <p>
 * This view implements {@link HasDynamicTitle}, so Flow shows the resolved
 * person name as the browser tab title. The breadcrumb, however, cannot use it:
 * {@code BreadcrumbBar} resolves every crumb from its route <em>class</em>, and
 * {@code HasDynamicTitle#getPageTitle()} only exists on a live instance the
 * class-based walker never hands out. Worse, a view that implements
 * {@code HasDynamicTitle} cannot also declare {@code @PageTitle} (Flow throws
 * {@code DuplicateNavigationTitleException}), so the class-based title resolver
 * has nothing to read and falls back to the bare class name —
 * {@code UserProfileView}. That ugly leaf crumb is the gap, on purpose; see
 * gap 3 in {@code API-GAPS.md}.
 */
@Route(value = "uc3/:userId", layout = MainLayout.class)
public class UserProfileView extends VerticalLayout
        implements BeforeEnterObserver, HasDynamicTitle {

    private final H1 heading = new H1();
    private String userName = "Unknown user";

    public UserProfileView() {
        add(new BreadcrumbBar());
        add(heading);
        add(new Paragraph(
                "The H1 and the browser tab title show this person's name "
                        + "(via HasDynamicTitle), but the breadcrumb leaf above "
                        + "reads \"UserProfileView\" — the class name. The "
                        + "class-based breadcrumb cannot reach a per-instance "
                        + "dynamic title, and this view carries no @PageTitle to "
                        + "fall back to (a view cannot declare both). "
                        + "RouteHierarchy still resolved the Users ancestor "
                        + "correctly."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String userId = event.getRouteParameters().get("userId").orElse("?");
        userName = Directory.nameOf(userId);
        heading.setText(userName);
    }

    @Override
    public String getPageTitle() {
        return userName;
    }
}
