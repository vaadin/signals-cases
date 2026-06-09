package com.example.uc6;

import java.util.List;
import java.util.Map;

import com.example.views.BreadcrumbBar;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.RouterLink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = DashboardView.class)
class LayoutWideBreadcrumbsTest extends SpringBrowserlessTest {

    @Test
    void sharedBarRendersForTheRootChild() {
        navigate(DashboardView.class);
        assertEquals("Dashboard", trail());
        assertTrue(crumbLinks().isEmpty());
    }

    @Test
    void sharedBarTracksDeeperChildrenIncludingDynamicLeaf() {
        navigate(MemberView.class, Map.of("member", "kim"));

        String trail = trail();
        assertTrue(trail.contains("Dashboard"), trail);
        assertTrue(trail.contains("Team"), trail);
        assertTrue(trail.endsWith("Kim Park"),
                "leaf must use the PageTitleGenerator label: " + trail);

        List<RouterLink> links = crumbLinks();
        assertEquals(2, links.size());
        assertEquals("Dashboard", links.get(0).getText());
        assertEquals("Team", links.get(1).getText());
    }

    @Test
    void counterRebuildsOncePerNavigationAndLayoutIsReused() {
        navigate(DashboardView.class);
        TeamLayout firstLayout = find(TeamLayout.class).single();
        int afterFirst = rebuildCount();

        navigate(TeamView.class);
        assertTrue(trail().contains("Team"));
        int afterSecond = rebuildCount();
        assertTrue(afterSecond > afterFirst,
                "routerStateSignal effect must rebuild the trail on navigation");

        navigate(MemberView.class, Map.of("member", "lee"));
        assertTrue(trail().endsWith("Lee Wong"));
        assertTrue(rebuildCount() > afterSecond);

        TeamLayout lastLayout = find(TeamLayout.class).single();
        assertEquals(firstLayout, lastLayout,
                "the parent layout instance must be reused across navigations");
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

    private int rebuildCount() {
        Span badge = find(Span.class).withId(TeamLayout.REBUILD_BADGE_ID)
                .single();
        String text = badge.getText();
        return Integer
                .parseInt(text.substring(text.lastIndexOf(':') + 1).trim());
    }
}
