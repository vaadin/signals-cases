package com.example.usecase26;

import com.example.MissingAPI;
import com.example.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Route(value = "use-case-26", layout = MainLayout.class)
@PageTitle("Use Case 26: Calendar with Days and Events as Signals")
@Menu(order = 26, title = "UC 26: Calendar with Days and Events")
@AnonymousAllowed
public class UseCase26View extends VerticalLayout {

    private final ListSignal<CalendarDay> allDays = new ListSignal<>();
    private final ValueSignal<Integer> daysToShow = new ValueSignal<>(365);
    private final ValueSignal<Integer> totalEventsCount = new ValueSignal<>(0);

    private static final String[] EVENT_COLORS = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F7DC6F", "#BB8FCE"};
    private static final Random random = new Random();
    private static final int MAX_DAYS = 3650; // Max 10 years

    public UseCase26View() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        var title = new H2("Use Case 26: Calendar with Days and Events as Signals");

        var description = new Paragraph(
                "This use case demonstrates a calendar that uses signals to manage days and events. "
                        + "Days are grouped into rows of 7 (one week), and VirtualList renders these rows for optimal performance. "
                        + "Each day card shows events stored in a ListSignal, with reactive UI updates. "
                        + "Partial weeks are aligned correctly within the grid. "
                        + "The calendar supports up to 10 years (3650 days) with virtualization ensuring smooth scrolling even with thousands of days and events.");

        // Add responsive CSS
        addResponsiveStyles();

        LocalDate startDate = LocalDate.now();
        for (int i = 0; i < MAX_DAYS; i++) {
            allDays.insertLast(new CalendarDay(startDate.plusDays(i)));
        }

        // Header with controls
        HorizontalLayout header = createHeader();
        add(title, description, header);

        // Create a computed signal for calendar rows (grouping items into rows of 7)
        Signal<List<CalendarRow>> calendarRowsSignal = Signal.computed(() -> {
            int visibleCount = daysToShow.get();
            var daysList = allDays.peek();
            List<CalendarRow> rows = new ArrayList<>();

            LocalDate currentMonth = null;
            List<CalendarItem> currentRowItems = new ArrayList<>();

            // Only render the first N days based on filter
            int daysToRender = Math.min(visibleCount, daysList.size());

            for (int i = 0; i < daysToRender; i++) {
                CalendarDay day = daysList.get(i).peek();
                LocalDate date = day.getDate();

                // Add month separator row
                if (currentMonth == null || !date.getMonth().equals(currentMonth.getMonth())) {
                    // Flush current row if not empty
                    if (!currentRowItems.isEmpty()) {
                        rows.add(new CalendarRow(currentRowItems));
                        currentRowItems = new ArrayList<>();
                    }

                    currentMonth = date;
                    rows.add(CalendarRow.monthSeparator(date));
                }

                boolean isWeekEnd = date.getDayOfWeek().getValue() == 7; // Sunday
                currentRowItems.add(CalendarItem.dayCard(day, isWeekEnd));

                // Create new row every 7 items or at week end
                if (currentRowItems.size() == 7 || isWeekEnd) {
                    rows.add(new CalendarRow(currentRowItems));
                    currentRowItems = new ArrayList<>();
                }
            }

            // Add remaining items
            if (!currentRowItems.isEmpty()) {
                rows.add(new CalendarRow(currentRowItems));
            }

            return rows;
        });

        // VirtualList with responsive grid
        VirtualList<CalendarRow> calendarGrid = new VirtualList<>();
        calendarGrid.addClassName("calendar-grid");
        calendarGrid.setRenderer(new ComponentRenderer<>(this::renderCalendarRow));

        MissingAPI.bindItems(calendarGrid, calendarRowsSignal);

        // Info box
        var infoBox = new Div();
        infoBox.getStyle().set("background-color", "#e0f7fa")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em").set("font-style", "italic");
        infoBox.add(new Paragraph(
                "This use case demonstrates a calendar that uses signals to manage days and events. "
                        + "Days are grouped into rows (weeks), and VirtualList virtualizes row rendering. "
                        + "A computed signal transforms the flat list of days into rows with month separators. "
                        + "Each day maintains its own ListSignal of events for reactive updates. "
                        + "The grid layout is responsive and adapts to different screen sizes."));

        add(calendarGrid, infoBox);
        expand(calendarGrid);
    }

    private Div renderCalendarRow(CalendarRow row) {
        if (row.isMonthSeparator()) {
            LocalDate monthDate = row.getMonthDate();
            if (monthDate != null) {
                return createMonthSeparator(monthDate);
            }
            // Fallback for null monthDate (shouldn't happen)
            return new Div();
        } else {
            Div rowContainer = new Div();
            rowContainer.addClassName("calendar-row");

            List<CalendarItem> items = row.getItems();

            // If not a full week, add empty spacers to align to the right
            if (!items.isEmpty() && items.size() < 7) {
                // Get the day of week of the first item (1=Monday, 7=Sunday)
                int firstDayOfWeek = items.get(0).getDay().getDate().getDayOfWeek().getValue();
                // Calculate how many empty spaces we need at the start
                int emptySpacesNeeded = firstDayOfWeek - 1; // Monday=0 empty, Tuesday=1 empty, etc.

                // Add empty divs for alignment
                for (int i = 0; i < emptySpacesNeeded; i++) {
                    Div emptySpacer = new Div();
                    emptySpacer.addClassName("day-card-spacer");
                    rowContainer.add(emptySpacer);
                }
            }

            for (CalendarItem item : items) {
                Div cardWrapper = new Div();
                cardWrapper.addClassName("day-card-wrapper");
                if (item.isWeekEnd()) {
                    cardWrapper.addClassName("week-end");
                }
                cardWrapper.add(createDayCard(item.getDay()));
                rowContainer.add(cardWrapper);
            }

            return rowContainer;
        }
    }

    private Div createMonthSeparator(LocalDate date) {
        Div separator = new Div();
        separator.addClassName("month-separator");
        separator.setText(date.getMonth().toString() + " " + date.getYear());
        return separator;
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding", "8px");

        // Days ahead control
        IntegerField daysAheadField = new IntegerField("Days Ahead");
        daysAheadField.setWidth("120px");
        daysAheadField.setPlaceholder("365");
        daysAheadField.bindValue(daysToShow, days -> {
            if (days > 0 && days <= MAX_DAYS) {
                daysToShow.set(days);
            }
        });

        Button addRandomEvents = new Button("Add Random Events", VaadinIcon.PLUS.create());
        addRandomEvents.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addRandomEvents.addClickListener(e -> {
            addRandomEventsToCalendar();
            updateEventCount();
        });

        Button clearAllEvents = new Button("Clear All", VaadinIcon.TRASH.create());
        clearAllEvents.addThemeVariants(ButtonVariant.LUMO_ERROR);
        clearAllEvents.addClickListener(e -> {
            clearAllEvents();
            updateEventCount();
        });

        // Total events counter
        Span eventCount = new Span();
        eventCount.bindText(totalEventsCount.map(count -> "Total Events: " + count));
        eventCount.getStyle().set("font-weight", "bold");

        Span daysCount = new Span();
        daysCount.bindText(daysToShow.map(count -> "Showing: " + count + "/" + MAX_DAYS));
        daysCount.getStyle().set("font-weight", "bold");

        header.add(daysAheadField, addRandomEvents, clearAllEvents, daysCount, eventCount);

        return header;
    }

    private void updateEventCount() {
        int total = 0;
        var daysList = allDays.peek();
        for (var daySignal : daysList) {
            CalendarDay day = daySignal.peek();
            total += day.getEvents().peek().size();
        }
        totalEventsCount.set(total);
    }

    private Div createDayCard(CalendarDay day) {
        Div card = new Div();
        card.getStyle()
                .set("border", "1px solid #ddd")
                .set("border-radius", "8px")
                .set("padding", "10px")
                .set("background", "white")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "6px")
                .set("height", "clamp(150px, 25vh, 300px)")
                .set("min-width", "0")
                .set("max-width", "100%")
                .set("overflow", "hidden");

        // Weekday header with badge - flex row with space between
        Div weekdayHeader = new Div();
        weekdayHeader.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("flex-shrink", "0")
                .set("min-width", "0");

        String weekday = day.getDate().getDayOfWeek().toString();
        weekday = weekday.charAt(0) + weekday.substring(1).toLowerCase();

        Span weekdayLabel = new Span(weekday);
        weekdayLabel.getStyle()
                .set("font-size", "10px")
                .set("font-weight", "600")
                .set("text-transform", "uppercase")
                .set("color", "#666")
                .set("letter-spacing", "0.5px");

        // Event count badge - only show if events exist, color changes with count
        Span eventBadge = new Span();

        Signal<@NonNull Integer> eventCount = day.getEvents().map(List::size);

        eventBadge.bindText(eventCount.map(count -> {
            if (count > 0) {
                return String.valueOf(count);
            } else {
                return "";
            }
        }));
        eventBadge.bindVisible(eventCount.map(count -> count > 0));
        eventBadge.getStyle().bind("background", eventCount.map(count -> {
            if (count > 0) {
                int darkness = Math.min(200, 50 + (count * 15));
                return String.format("rgb(%d, %d, %d)",
                    Math.max(0, 255 - darkness),
                    Math.max(0, 180 - darkness),
                    Math.max(0, 100 - darkness));
            } else {
                return "transparent";
            }
        }));

        eventBadge.getStyle()
                .set("color", "white")
                .set("border-radius", "50%")
                .set("width", "20px")
                .set("height", "20px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "10px")
                .set("font-weight", "bold")
                .set("flex-shrink", "0");

        weekdayHeader.add(weekdayLabel, eventBadge);
        card.add(weekdayHeader);

        // Date text
        Span dateText = new Span(day.getDate().toString());
        dateText.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "clamp(12px, 1.5vw, 14px)")
                .set("color", "#333")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap")
                .set("flex-shrink", "0");

        card.add(dateText);

        // Events container - takes remaining space, hides on small screens
        Div eventsContainer = new Div();
        eventsContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "4px")
                .set("flex", "1")
                .set("overflow-y", "auto")
                .set("overflow-x", "hidden")
                .set("min-height", "0");
        eventsContainer.addClassName("events-container");

        // Bind events to container using Signal.effect
        Signal.effect(eventsContainer, () -> {
            eventsContainer.removeAll();
            var events = day.getEvents().get();
            for (var eventSignal : events) {
                eventsContainer.add(createEventBadge(eventSignal.peek(), eventSignal, day));
            }
        });

        card.add(eventsContainer);

        // Add event button - always at bottom
        Button addEventBtn = new Button(VaadinIcon.PLUS_CIRCLE_O.create());
        addEventBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        addEventBtn.addClickListener(e -> {
            String eventTitle = "Event " + (day.getEvents().peek().size() + 1);
            String color = EVENT_COLORS[random.nextInt(EVENT_COLORS.length)];
            day.getEvents().insertLast(new CalendarEvent(eventTitle, color));
            updateEventCount();
        });
        addEventBtn.getStyle()
                .set("width", "100%")
                .set("min-height", "30px")
                .set("flex-shrink", "0")
                .set("margin-top", "auto");

        card.add(addEventBtn);

        return card;
    }

    private Div createEventBadge(CalendarEvent event, ValueSignal<CalendarEvent> eventSignal, CalendarDay day) {
        Div badge = new Div();
        badge.getStyle()
                .set("background", event.getColor())
                .set("color", "white")
                .set("padding", "8px 10px")
                .set("border-radius", "6px")
                .set("font-size", "13px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("gap", "8px")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.2)")
                .set("transition", "all 0.2s ease")
                .set("min-width", "0")
                .set("flex-shrink", "0");

        // Editable title field
        TextField titleField = new TextField();
        titleField.bindValue(event.getTitle(), event.getTitle()::set);
        titleField.getStyle()
                .set("flex", "1")
                .set("min-width", "0")
                .set("color", "white")
                .set("padding", "0")
                .set("margin", "0")
                .set("font-size", "13px")
                .set("font-weight", "500");
        titleField.addThemeVariants(
                com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_SMALL
        );

        // Remove all default input styles
        titleField.getElement().getStyle()
                .set("--lumo-text-field-size", "var(--lumo-size-xs)")
                .set("--vaadin-input-field-background", "transparent")
                .set("--vaadin-input-field-border-color", "transparent")
                .set("--vaadin-input-field-hover-highlight", "transparent")
                .set("--vaadin-input-field-invalid-background", "transparent");

        // Style when focused vs unfocused
        titleField.addFocusListener(focus -> {
            badge.getStyle()
                    .set("box-shadow", "0 2px 8px rgba(0,0,0,0.3)")
                    .set("transform", "scale(1.02)")
                    .set("outline", "2px solid rgba(255,255,255,0.3)")
                    .set("outline-offset", "2px");
        });

        titleField.addBlurListener(blur -> {
            badge.getStyle()
                    .set("box-shadow", "0 1px 3px rgba(0,0,0,0.2)")
                    .set("transform", "scale(1)")
                    .set("outline", "none")
                    .set("outline-offset", "0");
        });

        Button removeBtn = new Button(VaadinIcon.CLOSE_SMALL.create());
        removeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        removeBtn.getStyle()
                .set("min-width", "auto")
                .set("padding", "0")
                .set("margin", "0")
                .set("width", "18px")
                .set("height", "18px")
                .set("color", "white")
                .set("opacity", "0.7")
                .set("transition", "opacity 0.2s ease")
                .set("flex-shrink", "0");
        removeBtn.getElement().getStyle()
                .set("--lumo-button-size", "var(--lumo-size-xs)");
        removeBtn.addClickListener(e -> {
            // Remove event from the day's events list using the signal
            day.getEvents().remove(eventSignal);
            updateEventCount();
        });

        // Hover effect for remove button
        removeBtn.getElement().addEventListener("mouseenter", e -> {
            removeBtn.getStyle().set("opacity", "1");
        });
        removeBtn.getElement().addEventListener("mouseleave", e -> {
            removeBtn.getStyle().set("opacity", "0.7");
        });

        badge.add(titleField, removeBtn);
        return badge;
    }

    private void addRandomEventsToCalendar() {
        Random rand = new Random();
        int eventsToAdd = 50 + rand.nextInt(50); // Add 50-100 events

        var daysList = allDays.peek();
        int visibleDays = Math.min(daysToShow.peek(), daysList.size());

        for (int i = 0; i < eventsToAdd; i++) {
            // Add events only to visible days
            int dayIndex = rand.nextInt(visibleDays);
            CalendarDay day = daysList.get(dayIndex).peek();
            String eventTitle = "Event " + (day.getEvents().peek().size() + 1);
            String color = EVENT_COLORS[rand.nextInt(EVENT_COLORS.length)];
            day.getEvents().insertLast(new CalendarEvent(eventTitle, color));
        }
    }

    private void clearAllEvents() {
        var daysList = allDays.peek();
        for (var daySignal : daysList) {
            CalendarDay day = daySignal.peek();
            ListSignal<CalendarEvent> eventsSignal = day.getEvents();
            // Clear all events by repeatedly removing first element
            while (!eventsSignal.peek().isEmpty()) {
                eventsSignal.remove(eventsSignal.peek().getFirst());
            }
        }
    }

    public static class CalendarEvent {
        private final String id;
        private final ValueSignal<String> title;
        private final String color;

        public CalendarEvent(String title, String color) {
            this.id = UUID.randomUUID().toString();
            this.title = new ValueSignal<>(title);
            this.color = color;
        }

        public String getId() {
            return id;
        }

        public ValueSignal<String> getTitle() {
            return title;
        }

        public String getColor() {
            return color;
        }
    }

    public static class CalendarDay {
        private final LocalDate date;
        private final ListSignal<CalendarEvent> events;

        public CalendarDay(LocalDate date) {
            this.date = date;
            this.events = new ListSignal<>();
        }

        public LocalDate getDate() {
            return date;
        }

        public ListSignal<CalendarEvent> getEvents() {
            return events;
        }
    }

    public static class CalendarItem {
        private final CalendarDay day;
        private final boolean isWeekEnd;

        // Factory method for day card
        public static CalendarItem dayCard(CalendarDay day, boolean isWeekEnd) {
            return new CalendarItem(day, isWeekEnd);
        }

        private CalendarItem(CalendarDay day, boolean isWeekEnd) {
            this.day = day;
            this.isWeekEnd = isWeekEnd;
        }

        public CalendarDay getDay() {
            return day;
        }

        public boolean isWeekEnd() {
            return isWeekEnd;
        }
    }

    public static class CalendarRow {
        private final List<CalendarItem> items;
        private final @Nullable LocalDate monthDate;
        private final boolean isMonthSeparator;

        // Factory method for month separator row
        public static CalendarRow monthSeparator(LocalDate date) {
            return new CalendarRow(List.of(), date, true);
        }

        // Constructor for regular row
        public CalendarRow(List<CalendarItem> items) {
            this.items = new ArrayList<>(items);
            this.monthDate = null;
            this.isMonthSeparator = false;
        }

        // Private constructor
        private CalendarRow(List<CalendarItem> items, @Nullable LocalDate monthDate, boolean isMonthSeparator) {
            this.items = items;
            this.monthDate = monthDate;
            this.isMonthSeparator = isMonthSeparator;
        }

        public List<CalendarItem> getItems() {
            return items;
        }

        public @Nullable LocalDate getMonthDate() {
            return monthDate;
        }

        public boolean isMonthSeparator() {
            return isMonthSeparator;
        }
    }

    private void addResponsiveStyles() {
        // Add inline styles for responsive behavior
        getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.textContent = `" +
                        "  .calendar-grid {" +
                        "    padding: 16px;" +
                        "    overflow-y: auto;" +
                        "    overflow-x: hidden;" +
                        "    flex: 1;" +
                        "    width: 100%;" +
                        "    box-sizing: border-box;" +
                        "  }" +
                        "  .calendar-row {" +
                        "    display: grid;" +
                        "    gap: 12px;" +
                        "    grid-template-columns: repeat(7, 1fr);" +
                        "    margin-bottom: 12px;" +
                        "  }" +
                        "  .month-separator {" +
                        "    background: linear-gradient(to right, #007bff, #0056b3);" +
                        "    color: white;" +
                        "    padding: 12px 16px;" +
                        "    font-weight: bold;" +
                        "    font-size: 18px;" +
                        "    border-radius: 6px;" +
                        "    margin-top: 16px;" +
                        "    margin-bottom: 12px;" +
                        "    box-shadow: 0 2px 6px rgba(0,0,0,0.15);" +
                        "  }" +
                        "  .day-card-wrapper {" +
                        "    display: contents;" +
                        "  }" +
                        "  .week-end > * {" +
                        "    border-right: 3px solid #007bff !important;" +
                        "  }" +
                        "  @media (max-width: 1600px) {" +
                        "    .calendar-row { grid-template-columns: repeat(7, 1fr); }" +
                        "  }" +
                        "  @media (max-width: 1200px) {" +
                        "    .calendar-row { grid-template-columns: repeat(4, 1fr); }" +
                        "  }" +
                        "  @media (max-width: 900px) {" +
                        "    .calendar-row { grid-template-columns: repeat(3, 1fr); }" +
                        "    .events-container { max-height: 40px !important; }" +
                        "  }" +
                        "  @media (max-width: 600px) {" +
                        "    .calendar-row { grid-template-columns: repeat(2, 1fr); }" +
                        "    .events-container { display: none !important; }" +
                        "  }" +
                        "  @media (max-width: 400px) {" +
                        "    .calendar-row { grid-template-columns: 1fr; }" +
                        "  }" +
                        "`;" +
                        "document.head.appendChild(style);"
        );
    }
}
