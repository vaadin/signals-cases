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
 * UC3 — Dynamic leaf label (profile, {@code uc3/:userId}).
 * <p>
 * Implements {@link HasDynamicTitle} so the breadcrumb leaf shows the resolved
 * person name rather than the static {@code @PageTitle}. The parent
 * {@code uc3} is still found by stripping the {@code :userId} segment.
 */
@Route(value = "uc3/:userId", layout = MainLayout.class)
public class UserProfileView extends VerticalLayout
        implements BeforeEnterObserver, HasDynamicTitle {

    private final BreadcrumbBar breadcrumbs = new BreadcrumbBar();
    private final H1 heading = new H1();
    private String userName = "Unknown user";

    public UserProfileView() {
        add(breadcrumbs);
        add(heading);
        add(new Paragraph(
                "The breadcrumb leaf above matches this page's H1 — both come "
                        + "from getPageTitle(). This view carries no static "
                        + "@PageTitle at all (a view cannot declare both). "
                        + "RouteHierarchy resolved the Users ancestor; the "
                        + "dynamic title is the breadcrumb builder's own "
                        + "contribution for the current view."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String userId = event.getRouteParameters().get("userId").orElse("?");
        userName = Directory.nameOf(userId);
        heading.setText(userName);
        breadcrumbs.show(this, event.getRouteParameters());
    }

    @Override
    public String getPageTitle() {
        return userName;
    }
}
