package com.example.usecase11;

import jakarta.annotation.security.PermitAll;

import com.example.MissingAPI;
import com.example.MissingAPI.ComponentSize;
import com.example.views.MainLayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * Use Case 11: Responsive Layout with Container Size Signal
 *
 * Demonstrates responsive content inside a resizable container using a split
 * panel. The content adapts to the available width as you drag the splitter,
 * showing how components can be responsive within any container, not just the
 * window.
 *
 * Key Patterns: - ResizeObserver for container size tracking - Signal from
 * container resize events - Computed signals for breakpoint detection -
 * Responsive component visibility and layout within a fixed container - Split
 * panel with draggable divider
 */
@Route(value = "use-case-11", layout = MainLayout.class)
@PageTitle("Use Case 11: Responsive Layout")
@Menu(order = 11, title = "UC 11: Responsive Layout")
@PermitAll
public class UseCase11View extends VerticalLayout {

    private static final int SMALL_BREAKPOINT = 400;
    private static final int LARGE_BREAKPOINT = 700;

    private final Signal<ComponentSize> containerSizeSignal;
    private final Signal<Boolean> isSmall;
    private final Signal<Boolean> isMedium;
    private final Signal<Boolean> isLarge;

    private Div responsiveContent;

    public UseCase11View() {
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        // Create responsive content container first so we can set up the size
        // signal before building other panels that depend on it
        responsiveContent = new Div();
        responsiveContent.getStyle().set("padding", "1em")
                .set("height", "100%").set("overflow-y", "auto")
                .set("background-color", "#ffffff");
        containerSizeSignal = MissingAPI.sizeSignal(responsiveContent);
        isSmall = containerSizeSignal
                .map(size -> size.width() < SMALL_BREAKPOINT);
        isMedium = containerSizeSignal.map(size -> size
                .width() >= SMALL_BREAKPOINT
                && size.width() < LARGE_BREAKPOINT);
        isLarge = containerSizeSignal
                .map(size -> size.width() >= LARGE_BREAKPOINT);

        H2 title = new H2(
                "Use Case 11: Responsive Content in Resizable Container");

        Paragraph description = new Paragraph(
                "This use case demonstrates responsive content inside a resizable container. "
                        + "Drag the splitter left and right to resize the content area. "
                        + "The content adapts to the available width, showing different layouts at different sizes. "
                        + "This uses ResizeObserver to track container size changes.");

        // Create split layout
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(60); // Start at 60% for the primary
                                             // content

        // Left side: Static info panel
        Div infoPanel = createInfoPanel();

        // Right side: Populate responsive content
        populateResponsiveContent(responsiveContent);

        splitLayout.addToPrimary(responsiveContent);
        splitLayout.addToSecondary(infoPanel);

        // Info box
        Div tipBox = new Div();
        tipBox.getStyle().set("background-color", "#e0f7fa")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-bottom", "1em").set("font-style", "italic");
        tipBox.add(new Paragraph(
                "💡 Drag the splitter to resize the content area. "
                        + "The responsive content will adapt to different widths, "
                        + "showing mobile layout (< 400px), tablet layout (400-700px), or desktop layout (≥ 700px). "
                        + "This demonstrates container queries with Signals using ResizeObserver."));

        add(title, description, tipBox, splitLayout);
    }

    private Div createInfoPanel() {
        Div panel = new Div();
        panel.getStyle().set("padding", "1em")
                .set("background-color", "#f5f5f5").set("height", "100%")
                .set("overflow-y", "auto");

        H3 title = new H3("Info Panel");
        title.getStyle().set("margin-top", "0");

        Paragraph info = new Paragraph(
                "This is a static side panel. The main content on the left adapts "
                        + "to its container size as you drag the splitter.");

        H3 statsTitle = new H3("Current Size");

        // Container size display
        Div sizeDisplay = new Div();
        sizeDisplay.getStyle().set("background-color", "#ffffff")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin", "1em 0");

        Paragraph widthPara = new Paragraph(() -> "Width: " + containerSizeSignal.get().width() + "px");
        widthPara.getStyle().set("font-family", "monospace").set("margin",
                "0.25em 0");

        Paragraph heightPara = new Paragraph(() -> "Height: " + containerSizeSignal.get().height() + "px");
        heightPara.getStyle().set("font-family", "monospace").set("margin",
                "0.25em 0");

        Paragraph breakpointPara = new Paragraph(() -> {
            if (isSmall.get())
                return "📱 Small (< 400px)";
            if (isMedium.get())
                return "💻 Medium (400-700px)";
            return "🖥️ Large (≥ 700px)";
        });
        breakpointPara.getStyle().set("font-weight", "bold").set("margin",
                "0.5em 0 0 0");

        sizeDisplay.add(widthPara, heightPara, breakpointPara);

        panel.add(title, info, statsTitle, sizeDisplay);
        return panel;
    }

    private void populateResponsiveContent(Div container) {
        // Small width content
        Div smallContent = createSection("📱 Small Width Layout",
                "This is the mobile view (width < 400px). Navigation is stacked vertically, "
                        + "and complex UI elements are simplified or hidden.",
                "#fff3e0");
        smallContent.bindVisible(isSmall);

        // Medium width content
        Div mediumContent = createSection("💻 Medium Width Layout",
                "This is the tablet view (400px ≤ width < 700px). Navigation can be horizontal, "
                        + "and more content is visible with a balanced layout.",
                "#f3e5f5");
        mediumContent.bindVisible(isMedium);

        // Large width content
        Div largeContent = createSection("🖥️ Large Width Layout",
                "This is the desktop view (width ≥ 700px). All features are visible, "
                        + "with multi-column layouts and detailed information.",
                "#e8f5e9");
        largeContent.bindVisible(isLarge);

        // Responsive card grid
        H3 cardGridTitle = new H3("Responsive Card Grid");
        Component cardGrid = createResponsiveCardGrid();

        container.add(smallContent, mediumContent, largeContent, cardGridTitle,
                cardGrid);
    }

    private Div createSection(String title, String content,
            String backgroundColor) {
        Div section = new Div();
        section.getStyle().set("background-color", backgroundColor)
                .set("padding", "1.5em").set("border-radius", "8px")
                .set("margin", "0.5em 0");

        H3 sectionTitle = new H3(title);
        sectionTitle.getStyle().set("margin-top", "0").set("margin-bottom",
                "0.5em");

        Paragraph sectionContent = new Paragraph(content);
        sectionContent.getStyle().set("margin", "0");

        section.add(sectionTitle, sectionContent);
        return section;
    }

    private Component createResponsiveCardGrid() {
        Div gridContainer = new Div();

        // Create sample cards
        for (int i = 1; i <= 6; i++) {
            Div card = new Div();
            card.getStyle().set("background-color", "#f9f9f9")
                    .set("border", "1px solid #e0e0e0")
                    .set("border-radius", "4px").set("padding", "1em")
                    .set("margin", "0.5em").set("flex", "1 1 150px")
                    .set("min-width", "120px");

            H3 cardTitle = new H3("Card " + i);
            cardTitle.getStyle().set("margin", "0 0 0.5em 0").set("font-size",
                    "1em");

            Paragraph cardContent = new Paragraph("Content item " + i);
            cardContent.getStyle().set("margin", "0").set("font-size",
                    "0.875em");

            card.add(cardTitle, cardContent);
            gridContainer.add(card);
        }

        // Set responsive flex layout based on container size
        gridContainer.getStyle().set("display", "flex").set("margin", "1em 0");

        gridContainer.getStyle().bind("flex-direction", () -> isSmall.get() ? "column" : "row");
        gridContainer.getStyle().bind("flex-wrap", () -> isSmall.get() ? "nowrap" : "wrap");

        return gridContainer;
    }

}
