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
    void leafUsesGeneratorTitleNotClassName() {
        navigate(UserProfileView.class, Map.of("userId", "ada"));

        // The H1 and the breadcrumb leaf both resolve the person's name...
        assertEquals("Ada Lovelace", heading());
        String trail = trail();
        assertTrue(trail.contains("Users"), trail);
        assertTrue(trail.endsWith("Ada Lovelace"),
                "leaf must use the PageTitleGenerator label: " + trail);
        // ...and never fall back to the bare class name.
        assertFalse(trail.contains("UserProfileView"),
                "leaf must not show the class name: " + trail);

        List<RouterLink> links = crumbLinks();
        assertEquals(1, links.size());
        assertEquals("Users", links.get(0).getText());
    }

    @Test
    void leafLabelTracksTheRouteParameter() {
        navigate(UserProfileView.class, Map.of("userId", "grace"));
        assertEquals("Grace Hopper", heading());
        assertTrue(trail().endsWith("Grace Hopper"), trail());
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
