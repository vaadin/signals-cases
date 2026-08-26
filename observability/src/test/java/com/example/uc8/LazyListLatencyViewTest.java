package com.example.uc8;

import java.util.List;

import com.example.home.HomeView;
import com.example.uc8.LazyListLatencyView.Row;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.IntegerField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = { LazyListLatencyView.class, HomeView.class })
class LazyListLatencyViewTest extends SpringBrowserlessTest {

    @Autowired
    MeterRegistry registry;

    @Test
    void rendersTheComboBoxAndTheMeterTable() {
        navigate(LazyListLatencyView.class);

        assertEquals("UC8 — Why is this lazy list slow?",
                findInView(H1.class).first().getText());
        assertNotNull(findInView(ComboBox.class).first(),
                "the lazy combo box is the subject of the use case");
        assertNotNull(findInView(IntegerField.class).first(),
                "the backend delay has to be adjustable to make the cost "
                        + "visible");
        assertNotNull(findInView(Button.class).first());
    }

    @Test
    void theMeterTableNamesTheFourDataQueryMeters() {
        navigate(LazyListLatencyView.class);

        @SuppressWarnings("unchecked")
        Grid<Row> grid = findInView(Grid.class).first();
        List<String> names = grid.getGenericDataView().getItems()
                .map(Row::meter).toList();

        assertEquals(List.of("vaadin.data.count.duration",
                "vaadin.data.fetch.duration", "vaadin.data.fetch.requested",
                "vaadin.data.fetch.rows"), names,
                "the view exists to show these four meters");
    }

    @Test
    void theProviderIsSlowEnoughToBeWorthMeasuring() {
        navigate(LazyListLatencyView.class);

        IntegerField delay = findInView(IntegerField.class).first();
        assertTrue(delay.getValue() > 0,
                "a zero default delay would make the use case show nothing");
    }

    @Test
    void anUnmeasuredMeterReadsAsADashRatherThanZero() {
        // The registry is shared across the tests in this context, so this
        // asserts the formatting rule rather than that nothing has run yet.
        navigate(LazyListLatencyView.class);

        @SuppressWarnings("unchecked")
        Grid<Row> grid = findInView(Grid.class).first();
        List<Row> rows = grid.getGenericDataView().getItems().toList();

        assertFalse(rows.isEmpty());
        assertTrue(
                rows.stream().filter(row -> row.count() == 0)
                        .allMatch(row -> "—".equals(row.value())),
                "a meter with no recordings shows a dash, not 0 ms");
    }
}
