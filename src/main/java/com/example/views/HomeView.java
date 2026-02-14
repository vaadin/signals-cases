package com.example.views;

import jakarta.annotation.security.PermitAll;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Signal API Use Cases - Home")
@Menu(order = 0, title = "Home")
@PermitAll
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        // Header
        H1 header = new H1("Vaadin Signal API Use Cases");

        // Introduction
        Paragraph intro = new Paragraph(
                "This application demonstrates 28 use cases (22 single-user + 6 multi-user) for the Vaadin Signal API. "
                        + "Each use case validates different aspects of the reactive programming model, from basic "
                        + "one-way bindings to complex forms, AI integration, and multi-user collaboration.");

        // Signal API Overview
        H2 overviewTitle = new H2("What are Signals?");
        Paragraph overview = new Paragraph(
                "Signals are reactive state holders that automatically notify dependent computations and UI bindings "
                        + "when their values change. The framework manages the dependency graph and lifecycle automatically, "
                        + "eliminating manual listener management and cleanup code.");

        // Purpose
        H2 purposeTitle = new H2("Purpose of These Use Cases");
        Paragraph purpose = new Paragraph(
                "These use cases serve to validate the signal API design, identify potential gaps, provide practical "
                        + "examples for developers, and guide implementation priorities for the Vaadin team.");

        // Statistics
        H2 statsTitle = new H2("Collection Overview");
        VerticalLayout stats = new VerticalLayout();
        stats.setSpacing(false);
        stats.setPadding(false);
        stats.add(
                createStat("28",
                        "Total use cases (22 single-user + 6 multi-user)"),
                createStat("22",
                        "Single-user use cases across 13 categories"),
                createStat("6",
                        "Multi-user collaboration use cases"));

        // Categories
        H2 categoriesTitle = new H2("Use Case Categories");
        VerticalLayout categories = new VerticalLayout();
        categories.setSpacing(false);
        categories.setPadding(false);
        categories.add(
                createCategory("Basic Reactivity",
                        "UC01-02",
                        "Dynamic button state, progressive disclosure with nested conditions"),
                createCategory("SVG / Graphics",
                        "UC03",
                        "Interactive SVG shape editor with attribute binding"),
                createCategory("Lists & Grids",
                        "UC04, UC06-07",
                        "Filtered data grid, shopping cart, master-detail invoice"),
                createCategory("Forms",
                        "UC05, UC08-09",
                        "Cascading selector, multi-step wizard, Binder integration"),
                createCategory("Browser Integration",
                        "UC11-13",
                        "Responsive layout, dynamic view title, current user signal"),
                createCategory("Async & Search",
                        "UC14-16",
                        "Async data loading, debounced search, URL state integration"),
                createCategory("Complex State & AI",
                        "UC17-18",
                        "Custom PC builder (~70 signals), LLM-powered task management"),
                createCategory("Parallel Loading & Preferences",
                        "UC19-20",
                        "Parallel data loading with individual spinners, user preferences"),
                createCategory("i18n, Mapping & Dashboard",
                        "UC21-23",
                        "Reactive i18n, two-way mapped signals, real-time dashboard"),
                createCategory("Multi-User Collaboration",
                        "MUC01-04, MUC06-07",
                        "Chat, cursors, click race game, locking, shared tasks, shared LLM tasks"));

        // Documentation Link
        H2 docsTitle = new H2("Documentation");
        Paragraph docs = new Paragraph();
        docs.add(new Span("For detailed documentation, see "));
        docs.add(new Anchor("https://github.com/vaadin/platform/issues/8366",
                "vaadin/platform#8366 - Signal API Proposal"));
        docs.add(new Span(" and the "));
        docs.add(new Anchor(
                "https://github.com/vaadin/signals-cases/blob/main/signal-use-cases.md",
                "signal-use-cases.md"));
        docs.add(new Span(" file in this project."));

        // Getting Started
        H2 gettingStartedTitle = new H2("Getting Started");
        Paragraph gettingStarted = new Paragraph(
                "Use the navigation menu on the left to explore each use case. Each view demonstrates a specific "
                        + "pattern or feature of the signal API. The code shows how the proposed API would be used in "
                        + "real-world scenarios.");

        add(header, intro, new Hr(), overviewTitle, overview, purposeTitle,
                purpose, new Hr(), statsTitle, stats, new Hr(), categoriesTitle,
                categories, new Hr(), docsTitle, docs, new Hr(),
                gettingStartedTitle, gettingStarted);
    }

    private Span createStat(String number, String description) {
        Span stat = new Span();
        Span num = new Span(number);
        num.getStyle().set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)")
                .set("margin-right", "var(--lumo-space-s)");
        Span desc = new Span(description);
        desc.getStyle().set("color", "var(--lumo-secondary-text-color)");
        stat.add(num, desc);
        stat.getStyle().set("display", "flex").set("align-items", "center");
        return stat;
    }

    private VerticalLayout createCategory(String title, String range,
            String description) {
        VerticalLayout category = new VerticalLayout();
        category.setSpacing(false);
        category.setPadding(false);
        category.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-weight", "600");

        Span rangeSpan = new Span(range);
        rangeSpan.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-left", "var(--lumo-space-s)");

        Paragraph descPara = new Paragraph(description);
        descPara.getStyle().set("margin-top", "var(--lumo-space-xs)")
                .set("margin-bottom", "0")
                .set("color", "var(--lumo-secondary-text-color)");

        category.add(new Span(titleSpan, rangeSpan), descPara);

        return category;
    }
}
