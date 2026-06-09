package com.example.uc3;

import java.util.List;
import java.util.Map;

import com.example.views.BreadcrumbBar;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.RouterLink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UsersView.class)
class DynamicLeafLabelTest extends SpringBrowserlessTest {

    @Test
    void leafFallsBackToClassNameNotDynamicTitle() {
        navigate(UserProfileView.class, Map.of("userId", "ada"));

        // The view resolves the dynamic name into its H1 / browser tab title...
        assertEquals("Ada Lovelace", heading());

        // ...but the class-based breadcrumb cannot reach that per-instance
        // dynamic title, so the leaf shows the bare class name (the gap).
        String trail = trail();
        assertTrue(trail.contains("Users"), trail);
        assertTrue(trail.endsWith("UserProfileView"),
                "leaf must fall back to the class name: " + trail);
        assertFalse(trail.contains("Ada Lovelace"),
                "dynamic title must NOT reach the breadcrumb: " + trail);

        List<RouterLink> links = crumbLinks();
        assertEquals(1, links.size());
        assertEquals("Users", links.get(0).getText());
    }

    @Test
    void breadcrumbLeafDoesNotTrackTheRouteParameter() {
        navigate(UserProfileView.class, Map.of("userId", "grace"));

        // The H1 tracks the parameter; the breadcrumb leaf stays static.
        assertEquals("Grace Hopper", heading());
        String trail = trail();
        assertTrue(trail.endsWith("UserProfileView"), trail);
        assertFalse(trail.contains("Grace Hopper"), trail);
    }

    private String heading() {
        return findInView(H1.class).single().getText();
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
