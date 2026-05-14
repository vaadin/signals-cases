package com.example.views;

import com.example.common.AppCatalog;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.menu.MenuConfiguration;

@PageTitle("Text Selection API Use Cases")
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final Anchor sourceCodeLink;

    public MainLayout() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("Text Selection API Use Cases");
        title.addClassName("app-title");

        addToNavbar(toggle, title);

        // Fixed-position source code link overlay
        Div sourceCodeContainer = new Div();
        sourceCodeContainer.getStyle().set("position", "fixed").set("top",
                "calc(var(--vaadin-app-layout-navbar-offset-top) + 0.5em)")
                .set("right", "1em").set("z-index", "100")
                .set("pointer-events", "auto");

        Icon codeIcon = VaadinIcon.CODE.create();
        codeIcon.setSize("16px");
        codeIcon.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("margin-right", "0.5em");

        sourceCodeLink = new Anchor("", "View source");
        sourceCodeLink.setTarget("_blank");
        sourceCodeLink.getStyle().set("display", "inline-flex")
                .set("align-items", "center")
                .set("background-color", "rgba(255, 255, 255, 0.95)")
                .set("padding", "0.5em 0.75em").set("border-radius", "4px")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("text-decoration", "none")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("transition", "box-shadow 0.2s");

        sourceCodeLink.getElement().addEventListener("mouseenter", e -> {
            sourceCodeLink.getStyle().set("box-shadow",
                    "0 4px 8px rgba(0, 0, 0, 0.15)");
        }).addEventData("event.preventDefault");

        sourceCodeLink.getElement().addEventListener("mouseleave", e -> {
            sourceCodeLink.getStyle().set("box-shadow",
                    "0 2px 4px rgba(0, 0, 0, 0.1)");
        }).addEventData("event.preventDefault");

        Span linkContent = new Span(codeIcon);
        linkContent.add("View source");
        sourceCodeLink.removeAll();
        sourceCodeLink.add(linkContent);

        sourceCodeContainer.add(sourceCodeLink);
        getElement().appendChild(sourceCodeContainer.getElement());

        addToDrawer(AppCatalog.createSelector("text-selection"));

        SideNav nav = new SideNav();
        MenuConfiguration.getMenuEntries().forEach(entry -> nav
                .addItem(new SideNavItem(entry.title(), entry.path())));
        addToDrawer(nav);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        updateSourceCodeLink(event.getNavigationTarget());
    }

    private void updateSourceCodeLink(Class<?> viewClass) {
        if (viewClass == null) {
            return;
        }

        String className = viewClass.getSimpleName();
        String packageName = viewClass.getPackageName();

        String packagePath = packageName.replace(".", "/");
        String githubUrl = "https://github.com/vaadin/use-cases/tree/main/text-selection/src/main/java/"
                + packagePath + "/" + className + ".java";
        sourceCodeLink.setHref(githubUrl);

        boolean isViewClass = className.endsWith("View");
        sourceCodeLink.setVisible(isViewClass);
    }
}
