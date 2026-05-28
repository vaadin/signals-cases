package com.example.uc6;

import com.example.views.BreadcrumbBar;
import com.example.views.MainLayout;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.signals.Signal;

/**
 * UC6 — Layout-wide auto breadcrumbs.
 * <p>
 * One {@link BreadcrumbBar} lives in this parent layout, shared by every child
 * view ({@code uc6}, {@code uc6/team}, {@code uc6/team/:member}). The child
 * views build no breadcrumb of their own.
 * <p>
 * The layout subscribes to {@link UI#routerStateSignal()} from its constructor:
 * a single {@link Signal#effect} runs on the first attach (seeding the initial
 * trail) and again on every subsequent navigation, without any
 * {@code AfterNavigationObserver} registration or manual seeding step. The
 * {@link BreadcrumbBar} itself is also signal-bound, so the only thing this
 * effect adds is the rebuild counter — kept here to make the per-navigation
 * fire visible from the tests.
 */
@ParentLayout(MainLayout.class)
public class TeamLayout extends Div implements RouterLayout {

    public static final String REBUILD_BADGE_ID = "uc6-rebuilds";

    private final BreadcrumbBar breadcrumbs = new BreadcrumbBar();
    private final Span rebuildBadge = new Span();
    private final Div content = new Div();
    private int rebuilds;

    public TeamLayout() {
        addClassName("team-layout");
        rebuildBadge.setId(REBUILD_BADGE_ID);
        rebuildBadge.addClassName("rebuild-badge");
        Div header = new Div(breadcrumbs, rebuildBadge);
        header.addClassName("team-header");
        content.addClassName("team-content");
        add(header, content);

        Signal.effect(this, () -> {
            UI.getCurrent().routerStateSignal().get();
            rebuilds++;
            rebuildBadge.setText("breadcrumb rebuilds: " + rebuilds);
        });
    }

    @Override
    public void showRouterLayoutContent(HasElement routerLayoutContent) {
        content.getElement().removeAllChildren();
        if (routerLayoutContent != null) {
            content.getElement().appendChild(routerLayoutContent.getElement());
        }
    }
}
