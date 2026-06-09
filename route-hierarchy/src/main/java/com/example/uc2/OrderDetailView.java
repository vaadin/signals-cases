package com.example.uc2;

import com.example.views.BreadcrumbBar;
import com.example.views.MainLayout;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParent;

/**
 * UC2 — {@code @RouteParent} override (detail, {@code order-detail/:orderId}).
 * <p>
 * The route sits at a top-level URL that shares no prefix with {@code uc2}, so
 * the {@code @RouteParent(OrdersView.class)} annotation is the only thing that
 * lets {@link com.vaadin.flow.router.RouteHierarchy} place it under Orders. The
 * leaf label is the static {@link PageTitle} "Order" — the class-based
 * breadcrumb resolves every crumb from its route class, so the concrete order
 * id shows in the heading, not the trail (see gap 3 in {@code API-GAPS.md}).
 */
@Route(value = "order-detail/:orderId", layout = MainLayout.class)
@RouteParent(OrdersView.class)
@PageTitle("Order")
public class OrderDetailView extends VerticalLayout
        implements BeforeEnterObserver {

    private final H1 heading = new H1("Order detail");

    public OrderDetailView() {
        add(new BreadcrumbBar());
        add(heading);
        add(new Paragraph(
                "This page's URL is order-detail/:orderId — there is no uc2/ "
                        + "prefix to walk. The breadcrumb above reads "
                        + "Orders › Order purely because of the "
                        + "@RouteParent(OrdersView.class) annotation on this "
                        + "class. Remove the annotation and the Orders crumb "
                        + "would disappear."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String orderId = event.getRouteParameters().get("orderId").orElse("?");
        heading.setText("Order #" + orderId);
    }
}
