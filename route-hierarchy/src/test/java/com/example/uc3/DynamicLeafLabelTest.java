package com.example.uc3;

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
@ViewPackages(classes = UsersView.class)
class DynamicLeafLabelTest extends SpringBrowserlessTest {

    @Test
    void leafUsesDynamicTitleNotStaticPageTitle() {
        navigate(UserProfileView.class, Map.of("userId", "ada"));

        String trail = trail();
        assertTrue(trail.contains("Users"), trail);
        assertTrue(trail.contains("Ada Lovelace"),
                "leaf must use the HasDynamicTitle label: " + trail);
        assertFalse(trail.contains("Profile"),
                "leaf must NOT fall back to the static @PageTitle: " + trail);

        List<RouterLink> links = crumbLinks();
        assertEquals(1, links.size());
        assertEquals("Users", links.get(0).getText());
    }

    @Test
    void leafLabelTracksTheRouteParameter() {
        navigate(UserProfileView.class, Map.of("userId", "grace"));
        assertTrue(trail().contains("Grace Hopper"), trail());
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
