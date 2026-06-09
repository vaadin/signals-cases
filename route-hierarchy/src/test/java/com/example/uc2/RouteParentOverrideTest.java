package com.example.uc2;

import java.util.List;
import java.util.Map;

import com.example.views.BreadcrumbBar;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.router.RouterLink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = OrdersView.class)
class RouteParentOverrideTest extends SpringBrowserlessTest {

    @Test
    void parentShowsOnlyItself() {
        navigate(OrdersView.class);
        assertEquals("Orders", trail());
        assertTrue(crumbLinks().isEmpty());
    }

    @Test
    void detailLinksBackToOrdersViaRouteParentAnnotation() {
        navigate(OrderDetailView.class, Map.of("orderId", "1001"));

        String trail = trail();
        assertTrue(trail.contains("Orders"),
                "Orders ancestor must come from @RouteParent, not the URL: "
                        + trail);
        assertTrue(trail.endsWith("Order"),
                "leaf must be the static @PageTitle label: " + trail);

        List<RouterLink> links = crumbLinks();
        assertEquals(1, links.size(),
                "trail must link exactly the Orders ancestor");
        assertEquals("Orders", links.get(0).getText());
    }

    @Test
    void detailLeafIsStaticRegardlessOfTheOrderParameter() {
        navigate(OrderDetailView.class, Map.of("orderId", "1002"));
        String trail = trail();
        assertTrue(trail.endsWith("Order"),
                "class-based breadcrumb shows the static @PageTitle: " + trail);
        assertFalse(trail.contains("1002"),
                "the order id must not reach the breadcrumb leaf: " + trail);
    }

    private String trail() {
        return find(BreadcrumbBar.class).single().getElement()
                .getTextRecursively();
    }

    private List<RouterLink> crumbLinks() {
        return find(BreadcrumbBar.class).single().getChildren()
                .filter(RouterLink.class::isInstance)
                .map(RouterLink.class::cast).toList();
    }
}
