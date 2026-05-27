package com.example.uc1;

import java.util.List;

import com.example.views.BreadcrumbBar;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.RouterLink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = SubcategoryView.class)
class UrlPrefixTrailTest extends SpringBrowserlessTest {

    @Test
    void rootShowsSingleCurrentCrumbWithNoLinks() {
        navigate(CatalogView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "Catalog".equals(h.getText())));
        assertEquals("Catalog", trail());
        assertTrue(crumbLinks().isEmpty(),
                "root breadcrumb must have no links (leaf is the current page)");
    }

    @Test
    void categoryShowsParentLinkAndCurrentPage() {
        navigate(CategoryView.class);

        assertTrue(trail().contains("Catalog"));
        assertTrue(trail().contains("Electronics"));

        List<RouterLink> links = crumbLinks();
        assertEquals(1, links.size(),
                "category trail must link only the Catalog ancestor");
        assertEquals("Catalog", links.get(0).getText());
    }

    @Test
    void leafWalksTwoUrlSegmentsToTheRoot() {
        navigate(SubcategoryView.class);

        String trail = trail();
        assertTrue(trail.contains("Catalog"), trail);
        assertTrue(trail.contains("Electronics"), trail);
        assertTrue(trail.contains("Laptops"), trail);

        List<RouterLink> links = crumbLinks();
        assertEquals(2, links.size(),
                "leaf trail must link both ancestors via URL-prefix walking");
        assertEquals("Catalog", links.get(0).getText());
        assertEquals("Electronics", links.get(1).getText());
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
