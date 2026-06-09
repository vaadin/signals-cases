package com.example.uc4;

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
@ViewPackages(classes = ProjectsView.class)
class ParameterPreservingTrailTest extends SpringBrowserlessTest {

    @Test
    void deepLeafBuildsFullFourLevelTrail() {
        navigate(TaskDetailView.class,
                Map.of("projectId", "apollo", "taskId", "2"));

        String trail = trail();
        assertTrue(trail.contains("Projects"), trail);
        // The Project ancestor crumb is itself dynamic (gap-5 win): it reads
        // "Project Apollo" from the :projectId, not a static "Project".
        assertTrue(trail.contains("Project Apollo"), trail);
        assertTrue(trail.contains("Tasks"), trail);
        assertTrue(trail.endsWith("Wire the backend"),
                "leaf must use the PageTitleGenerator label: " + trail);

        List<RouterLink> links = crumbLinks();
        assertEquals(3, links.size(),
                "leaf trail must link the three ancestors");
        assertEquals("Projects", links.get(0).getText());
        assertEquals("Project Apollo", links.get(1).getText());
        assertEquals("Tasks", links.get(2).getText());
    }

    @Test
    void ancestorLinksCarryOnlyTheParametersTheirTemplateNeeds() {
        navigate(TaskDetailView.class,
                Map.of("projectId", "apollo", "taskId", "2"));

        List<RouterLink> links = crumbLinks();
        String projectsHref = links.get(0).getHref();
        String projectHref = links.get(1).getHref();
        String tasksHref = links.get(2).getHref();

        // Root "uc4" has no parameters at all.
        assertFalse(projectsHref.contains("apollo"),
                "Projects root link must not carry :projectId: "
                        + projectsHref);

        // The two middle links keep :projectId so they stay in this project...
        assertTrue(projectHref.contains("apollo"), projectHref);
        assertTrue(tasksHref.contains("apollo"), tasksHref);

        // ...but neither leaks the deeper :taskId into the ancestor URLs.
        assertFalse(projectHref.endsWith("/2") || projectHref.contains("/2/"),
                "Project link must not carry :taskId: " + projectHref);
        assertTrue(tasksHref.endsWith("tasks"),
                "Tasks link must stop at the tasks segment: " + tasksHref);
    }

    @Test
    void midLevelProjectShowsTwoCrumbs() {
        navigate(ProjectView.class, Map.of("projectId", "zephyr"));

        String trail = trail();
        assertTrue(trail.contains("Projects"), trail);
        assertEquals(1, crumbLinks().size());
        assertEquals("Projects", crumbLinks().get(0).getText());
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
