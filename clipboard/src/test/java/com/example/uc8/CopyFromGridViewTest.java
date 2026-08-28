package com.example.uc8;

import java.util.Set;

import com.example.uc8.CopyFromGridView.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.html.H1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = CopyFromGridView.class)
class CopyFromGridViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersHeadingGridAndDisabledCopyButton() {
        CopyFromGridView view = navigate(CopyFromGridView.class);

        assertTrue(findInView(H1.class).all().stream().anyMatch(
                h -> "UC8 — Copy from a data grid".equals(h.getText())));
        assertEquals(500, test(view.grid).size());
        assertEquals("Name", test(view.grid).getHeaderCell(0));
        // Nothing selected yet, so there is nothing to copy.
        assertFalse(view.copySelection.isEnabled());
        assertEquals("", view.selectionSlot.getValue());
    }

    @Test
    void contextMenuStagesTheValuesOfTheTargetRow() {
        CopyFromGridView view = navigate(CopyFromGridView.class);

        Customer target = test(view.grid).getRow(3);
        // What the browser does on right-click: the server decides whether to
        // open the menu, and stages the row's values while doing so.
        assertTrue(view.contextMenu.getDynamicContentHandler().test(target));

        assertEquals(target.email(), view.emailSlot.getValue());
        assertEquals(target.phone(), view.phoneSlot.getValue());
        assertEquals(
                "Name\tCompany\tEmail\tPhone\n"
                        + String.join("\t", target.name(), target.company(),
                                target.email(), target.phone()),
                view.rowSlot.getValue());
    }

    @Test
    void rightClickOutsideRowsDoesNotOpenTheMenu() {
        CopyFromGridView view = navigate(CopyFromGridView.class);

        assertFalse(view.contextMenu.getDynamicContentHandler().test(null));
    }

    @Test
    void selectionIsStagedAsTsvInGridOrder() {
        CopyFromGridView view = navigate(CopyFromGridView.class);

        Customer first = test(view.grid).getRow(0);
        Customer second = test(view.grid).getRow(1);
        // Selected in reverse order — the copied table must still follow the
        // order the rows appear in, not the selection set's iteration order.
        view.grid.asMultiSelect().setValue(Set.of(second, first));

        assertEquals(
                "Name\tCompany\tEmail\tPhone\n"
                        + String.join("\t", first.name(), first.company(),
                                first.email(), first.phone())
                        + "\n"
                        + String.join("\t", second.name(), second.company(),
                                second.email(), second.phone()),
                view.selectionSlot.getValue());
        assertTrue(view.copySelection.isEnabled());
        assertEquals("Copy 2 selected rows", view.copySelection.getText());
    }

    @Test
    void clearingTheSelectionDisablesTheCopyButton() {
        CopyFromGridView view = navigate(CopyFromGridView.class);

        test(view.grid).select(0);
        assertTrue(view.copySelection.isEnabled());

        view.grid.deselectAll();

        assertFalse(view.copySelection.isEnabled());
        assertEquals("", view.selectionSlot.getValue());
        assertEquals("Copy selected rows", view.copySelection.getText());
    }
}
