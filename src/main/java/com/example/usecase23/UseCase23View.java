package com.example.usecase23;

import jakarta.annotation.security.PermitAll;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

import com.example.usecase23.ServiceHealth.Status;
import com.example.views.MainLayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEffect;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Marker;
import com.vaadin.flow.component.charts.model.PlotOptionsAreaspline;
import com.vaadin.flow.component.charts.model.PointPlacement;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

@PageTitle("Use Case 23: Real-time Dashboard")
@Route(value = "use-case-23", layout = MainLayout.class)
@Menu(order = 23, title = "UC 23: Real-time Dashboard")
@PermitAll
public class UseCase23View extends Main {

    private static final int TIMELINE_POINTS = 12;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm:ss");

    private final ValueSignal<Number> currentUsersSignal = new ValueSignal<>(
            745);
    private final ValueSignal<Number> viewEventsSignal = new ValueSignal<>(
            54600);
    private final ValueSignal<Number> conversionRateSignal = new ValueSignal<>(
            18);
    private final ValueSignal<Number> customMetricSignal = new ValueSignal<>(
            -123.45);

    private final Random random = new Random();
    private final List<HighlightCard> highlightCards = new ArrayList<>();
    private Chart viewEventsChart;
    private final ListSignal<String> timelineCategoriesSignal = new ListSignal<>();
    private final ListSignal<Number> berlinTimelineSignal = new ListSignal<>();
    private final ListSignal<Number> londonTimelineSignal = new ListSignal<>();
    private final ListSignal<Number> newYorkTimelineSignal = new ListSignal<>();
    private final ListSignal<Number> tokyoTimelineSignal = new ListSignal<>();
    private final ListSignal<ServiceHealth> serviceHealthSignal = new ListSignal<>();
    private Chart responseTimesChart;
    private DataSeries responseSeries;
    private final ListSignal<Number> responseSignal = new ListSignal<>();

    public UseCase23View() {
        addClassName("dashboard-view");

        Board board = new Board();
        board.addRow(
                createHighlightCard("Current users", currentUsersSignal,
                        this::formatNumber),
                createHighlightCard("View events", viewEventsSignal,
                        this::formatCompactNumber),
                createHighlightCard("Conversion rate", conversionRateSignal,
                        number -> String.format("%.1f%%",
                                number.doubleValue())),
                createHighlightCard("Custom metric", customMetricSignal,
                        this::formatNumber));
        board.addRow(createViewEvents());
        board.addRow(createServiceHealth(), createResponseTimes());
        add(board);

        addAttachListener(event -> {
            // TODO do not use polling for this
            UI ui = event.getUI();
            ui.setPollInterval(2000);
            ui.addPollListener(pollEvent -> updateMockData());
        });
    }

    private HighlightCard createHighlightCard(String title,
            ValueSignal<Number> signal, Function<Number, String> format) {

        HighlightCard card = new HighlightCard(title, signal, format);
        card.update(signal.get());
        highlightCards.add(card);
        return card;
    }

    private Component createViewEvents() {
        // Header
        HorizontalLayout header = createHeader("View events",
                "City / last 2 min");

        // Chart
        Chart chart = new Chart(ChartType.AREASPLINE);
        Configuration conf = chart.getConfiguration();
        conf.getChart().setStyledMode(true);

        XAxis xAxis = new XAxis();
        conf.addxAxis(xAxis);

        conf.getyAxis().setTitle("Values");

        PlotOptionsAreaspline plotOptions = new PlotOptionsAreaspline();
        plotOptions.setPointPlacement(PointPlacement.ON);
        plotOptions.setMarker(new Marker(false));
        conf.addPlotOptions(plotOptions);

        ListSeries berlinSeries = new ListSeries("Berlin", new Number[0]);
        ListSeries londonSeries = new ListSeries("London", new Number[0]);
        ListSeries newYorkSeries = new ListSeries("New York", new Number[0]);
        ListSeries tokyoSeries = new ListSeries("Tokyo", new Number[0]);

        bindData(chart, berlinSeries, berlinTimelineSignal);
        bindData(chart, londonSeries, londonTimelineSignal);
        bindData(chart, newYorkSeries, newYorkTimelineSignal);
        bindData(chart, tokyoSeries, tokyoTimelineSignal);

        ComponentEffect.effect(chart,
                () -> xAxis.setCategories(timelineCategoriesSignal.get()
                        .stream().map(Signal::get).toArray(String[]::new)));

        conf.addSeries(berlinSeries);
        conf.addSeries(londonSeries);
        conf.addSeries(newYorkSeries);
        conf.addSeries(tokyoSeries);

        viewEventsChart = chart;

        // Add it all together
        VerticalLayout viewEvents = new VerticalLayout(header, chart);
        viewEvents.setSpacing(false);
        viewEvents.getElement().getThemeList().add("spacing-l");
        return viewEvents;
    }

    private static void bindData(Chart chart, ListSeries series,
            ListSignal<Number> signal) {
        ComponentEffect.effect(chart, () -> {
            series.setData(signal.get().stream().map(Signal::get)
                    .toArray(Number[]::new));
            // TODO issue of getting the values from ListSignal instead of
            // Signal<Number>
        });
    }

    private Component createServiceHealth() {
        // Header
        HorizontalLayout header = createHeader("Service health",
                "Input / output");

        // Grid
        Grid<ValueSignal<ServiceHealth>> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setAllRowsVisible(true);

        grid.addColumn(new ComponentRenderer<>(signal -> {
            Span status = new Span();

            ComponentEffect.effect(status, () -> {
                ServiceHealth serviceHealth = signal.get();
                String statusTextInner = getStatusDisplayName(serviceHealth);
                status.getElement().setAttribute("aria-label",
                        "Status: " + statusTextInner);
                status.getElement().setAttribute("title",
                        "Status: " + statusTextInner);
                status.getElement().getThemeList().clear();
                status.getElement().getThemeList()
                        .add(getStatusTheme(serviceHealth));
            });
            return status;
        })).setHeader("").setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(signal -> signal.get().getCity()).setHeader("City")
                .setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(signal -> {
            var input = new Span(String.valueOf(signal.get().getInput()));

            ComponentEffect.effect(input, () -> input
                    .setText(String.valueOf(signal.get().getInput())));
            return input;
        })).setHeader("Input").setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(new ComponentRenderer<>(signal -> {
            var output = new Span(String.valueOf(signal.get().getOutput()));

            ComponentEffect.effect(output, () -> output
                    .setText(String.valueOf(signal.get().getOutput())));
            return output;
        })).setHeader("Output").setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

        // Add it all together
        VerticalLayout serviceHealth = new VerticalLayout(header, grid);

        ComponentEffect.effect(grid, () -> {
            grid.setItems(serviceHealthSignal.get());
            // TODO not this, update each signal individually
        });

        mockServiceHealth().forEach(serviceHealthSignal::insertLast);

        return serviceHealth;
    }

    private Component createResponseTimes() {
        HorizontalLayout header = createHeader("Response times",
                "Average across all systems");

        // Chart
        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        conf.getChart().setStyledMode(true);
        chart.setThemeName("gradient");
        responseTimesChart = chart;

        responseSeries = new DataSeries();
        responseSeries.add(new DataSeriesItem("System 1", 12.5));
        responseSeries.add(new DataSeriesItem("System 2", 12.5));
        responseSeries.add(new DataSeriesItem("System 3", 12.5));
        responseSeries.add(new DataSeriesItem("System 4", 12.5));
        responseSeries.add(new DataSeriesItem("System 5", 12.5));
        responseSeries.add(new DataSeriesItem("System 6", 12.5));
        conf.addSeries(responseSeries);

        ComponentEffect.effect(chart, () -> {
            var responseValues = responseSignal.get();
            responseSeries.get(0).setY(responseValues.get(0).get());
            responseSeries.get(1).setY(responseValues.get(1).get());
            responseSeries.get(2).setY(responseValues.get(2).get());
            responseSeries.get(3).setY(responseValues.get(3).get());
            responseSeries.get(4).setY(responseValues.get(4).get());
            responseSeries.get(5).setY(responseValues.get(5).get());
        });

        responseSignal.insertLast(12.5);
        responseSignal.insertLast(12.5);
        responseSignal.insertLast(12.5);
        responseSignal.insertLast(12.5);
        responseSignal.insertLast(12.5);
        responseSignal.insertLast(12.5);

        // Add it all together
        VerticalLayout serviceHealth = new VerticalLayout(header, chart);
        serviceHealth.setSpacing(false);
        return serviceHealth;
    }

    private HorizontalLayout createHeader(String title, String subtitle) {
        H2 h2 = new H2(title);
        h2.addClassNames(FontSize.XLARGE, Margin.NONE);

        Span span = new Span(subtitle);
        span.addClassNames(TextColor.SECONDARY, FontSize.XSMALL);

        VerticalLayout column = new VerticalLayout(h2, span);
        column.setPadding(false);
        column.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(column);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setSpacing(false);
        header.setWidthFull();
        return header;
    }

    private String getStatusDisplayName(ServiceHealth serviceHealth) {
        Status status = serviceHealth.getStatus();
        if (status == Status.OK) {
            return "Ok";
        } else if (status == Status.FAILING) {
            return "Failing";
        } else if (status == Status.EXCELLENT) {
            return "Excellent";
        } else {
            return status.toString();
        }
    }

    private String getStatusTheme(ServiceHealth serviceHealth) {
        Status status = serviceHealth.getStatus();
        String theme = "badge primary small";
        if (status == Status.EXCELLENT) {
            theme += " success";
        } else if (status == Status.FAILING) {
            theme += " error";
        }
        return theme;
    }

    private void updateMockData() {
        if (highlightCards.size() >= 4) {
            currentUsersSignal.set(randomBetween(650, 820));
            viewEventsSignal.set(randomBetween(42000, 62000));
            conversionRateSignal.set(randomBetween(12, 24));
            customMetricSignal.set(randomBetween(-200, 200));
        }

        if (berlinTimelineSignal.get().size() >= TIMELINE_POINTS) {
            berlinTimelineSignal.remove(berlinTimelineSignal.get().getFirst()); // TODO
                                                                                // github
                                                                                // issue
                                                                                // remove
                                                                                // first
                                                                                // /
                                                                                // last?
        }
        berlinTimelineSignal.insertLast(randomBetween(480, 920));

        if (londonTimelineSignal.get().size() >= TIMELINE_POINTS) {
            londonTimelineSignal.remove(londonTimelineSignal.get().getFirst());
        }
        londonTimelineSignal.insertLast(randomBetween(420, 820));

        if (newYorkTimelineSignal.get().size() >= TIMELINE_POINTS) {
            newYorkTimelineSignal
                    .remove(newYorkTimelineSignal.get().getFirst());
        }
        newYorkTimelineSignal.insertLast(randomBetween(220, 520));

        if (tokyoTimelineSignal.get().size() >= TIMELINE_POINTS) {
            tokyoTimelineSignal.remove(tokyoTimelineSignal.get().getFirst());
        }
        tokyoTimelineSignal.insertLast(randomBetween(260, 600));

        if (timelineCategoriesSignal.get().size() >= TIMELINE_POINTS) {
            timelineCategoriesSignal
                    .remove(timelineCategoriesSignal.get().getFirst());
        }
        timelineCategoriesSignal
                .insertLast(LocalTime.now().format(TIME_FORMATTER));

        if (viewEventsChart != null) {
            viewEventsChart.drawChart();
        }

        for (ValueSignal<Number> signal : responseSignal.get()) {
            signal.set(randomBetween(6, 22));
        }

        if (responseTimesChart != null) {
            responseTimesChart.drawChart();
        }

        // TODO report issue ListSignal set items / set value
        // serviceHealthSignal.clear();
        var healthValues = serviceHealthSignal.get();
        mockServiceHealth().forEach(
                newHealth -> healthValues.forEach(currentHealthSignal -> {
                    var currentHealt = currentHealthSignal.get();
                    if (Objects.equals(currentHealt.getCity(),
                            newHealth.getCity())) {
                        currentHealthSignal.set(newHealth);
                    }
                }));
    }

    private List<ServiceHealth> mockServiceHealth() {
        return List.of(
                new ServiceHealth(randomStatus(), "Münster",
                        randomBetween(280, 360), randomBetween(1200, 1700)),
                new ServiceHealth(randomStatus(), "Cluj-Napoca",
                        randomBetween(260, 340), randomBetween(1100, 1600)),
                new ServiceHealth(randomStatus(), "Ciudad Victoria",
                        randomBetween(240, 320), randomBetween(1000, 1500)));
    }

    private Status randomStatus() {
        int pick = random.nextInt(3);
        if (pick == 0) {
            return Status.EXCELLENT;
        } else if (pick == 1) {
            return Status.OK;
        }
        return Status.FAILING;
    }

    private int randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private String formatNumber(Number value) {
        return String.valueOf(value);
    }

    private String formatCompactNumber(Number value) {
        if (value.doubleValue() >= 1000) {
            double rounded = Math.round(value.doubleValue() / 100.0) / 10.0;
            return rounded + "k";
        }
        return String.valueOf(value);
    }

    private static final class HighlightCard extends VerticalLayout {
        private final Span valueSpan;
        private final Span badge;
        private final Function<Number, String> format;
        private Number lastNumeric;

        private HighlightCard(String title, ValueSignal<Number> signal,
                Function<Number, String> format) {
            this.format = format;

            H2 h2 = new H2(title);
            h2.addClassNames(FontWeight.NORMAL, Margin.NONE,
                    TextColor.SECONDARY, FontSize.XSMALL);

            valueSpan = new Span(format.apply(signal.get()));
            valueSpan.addClassNames(FontWeight.SEMIBOLD, FontSize.XXXLARGE);
            ComponentEffect.effect(valueSpan, () -> update(signal.get()));

            badge = new Span();

            add(h2, valueSpan, badge);
            getStyle().setGap("5px");
        }

        private void update(Number newValue) {
            valueSpan.setText(format.apply(newValue));
            badge.removeAll();

            VaadinIcon icon = VaadinIcon.ARROW_UP;
            String prefix = "";
            String theme = "badge";

            double percentage = calculatePercentageChange(newValue,
                    lastNumeric);
            if (percentage == 0) {
                prefix = "±";
            } else if (percentage > 0) {
                prefix = "+";
                theme += " success";
            } else {
                icon = VaadinIcon.ARROW_DOWN;
                theme += " error";
            }

            Icon i = icon.create();
            i.setSize("10px");
            i.getStyle().setMarginRight("4px").setMarginLeft("0");
            badge.add(i, new Span(prefix + percentage));
            badge.getElement().getThemeList().clear();
            badge.getElement().getThemeList().add(theme);

            lastNumeric = newValue;
        }

        private double calculatePercentageChange(Number current,
                Number previous) {
            if (previous == null) {
                return 0.0;
            }
            double percent = ((current.doubleValue() - previous.doubleValue())
                    / Math.abs(previous.doubleValue())) * 100.0;
            return Math.round(percent * 10.0) / 10.0;
        }
    }

}
