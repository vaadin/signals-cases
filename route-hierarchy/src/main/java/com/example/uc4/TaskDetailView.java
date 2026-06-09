package com.example.uc4;

import com.example.views.BreadcrumbBar;
import com.example.views.MainLayout;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

/**
 * UC4 — Parameter-preserving links (leaf,
 * {@code uc4/:projectId/tasks/:taskId}).
 * <p>
 * Walking up from here produces Projects › Project › Tasks › Task. The two
 * middle links must carry {@code :projectId} but not {@code :taskId}; the root
 * carries neither. {@code BreadcrumbBar} derives each subset from the
 * ancestor's own template. The leaf label is the static {@link PageTitle}
 * "Task"; the concrete task name shows in the heading, not the trail.
 */
@Route(value = "uc4/:projectId/tasks/:taskId", layout = MainLayout.class)
@PageTitle("Task")
public class TaskDetailView extends VerticalLayout
        implements BeforeEnterObserver {

    private final H1 heading = new H1();

    public TaskDetailView() {
        add(new BreadcrumbBar());
        add(heading);
        add(new Paragraph(
                "Hover the Project and Tasks crumbs above: both hrefs include "
                        + "this project's id, while the Projects root link has "
                        + "no parameter at all. None of that mapping is done by "
                        + "RouteHierarchy — it only handed back the ancestor "
                        + "classes."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        RouteParameters parameters = event.getRouteParameters();
        String taskId = parameters.get("taskId").orElse("?");
        heading.setText(ProjectData.taskName(taskId));
    }
}
