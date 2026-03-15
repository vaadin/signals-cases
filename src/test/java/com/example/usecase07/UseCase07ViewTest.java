package com.example.usecase07;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase07View.class)
@WithMockUser
class UseCase07ViewTest extends SpringBrowserlessTest {

    @SuppressWarnings("unchecked")
    @Test
    void gridShowsFourInvoices() {
        navigate(UseCase07View.class);
        runPendingSignalsTasks();

        Grid<Invoice> grid = $view(Grid.class).atIndex(1);
        assertEquals(4, test(grid).size());
    }

    @Test
    void newInvoiceButtonPresent() {
        navigate(UseCase07View.class);

        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "New Invoice".equals(b.getText())));
    }

    @Test
    void detailPanelHiddenInitially() {
        navigate(UseCase07View.class);
        runPendingSignalsTasks();

        // Detail panel uses bindVisible - when no invoice is selected,
        // "Add Line Item" button should not be visible
        assertFalse($view(Button.class).all().stream()
                .anyMatch(b -> "Add Line Item".equals(b.getText())));
    }

    @SuppressWarnings("unchecked")
    @Test
    void newInvoiceAddsRowToGrid() {
        navigate(UseCase07View.class);
        runPendingSignalsTasks();

        Button newInvoice = $view(Button.class).all().stream()
                .filter(b -> "New Invoice".equals(b.getText())).findFirst()
                .orElseThrow();
        test(newInvoice).click();
        runPendingSignalsTasks();

        Grid<Invoice> grid = $view(Grid.class).atIndex(1);
        assertEquals(5, test(grid).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void selectingInvoiceShowsDetailPanel() {
        navigate(UseCase07View.class);
        runPendingSignalsTasks();

        // Select first invoice in grid
        Grid<Invoice> grid = $view(Grid.class).atIndex(1);
        Invoice firstInvoice = test(grid).getRow(0);
        grid.asSingleSelect().setValue(firstInvoice);
        runPendingSignalsTasks();

        // Detail panel should become visible — "Add Line Item" button appears
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Add Line Item".equals(b.getText())));
    }
}
