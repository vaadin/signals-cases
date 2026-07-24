package com.example.uc5;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.clipboard.ClipboardSimulator;
import com.vaadin.flow.component.html.Div;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ViewPackages(classes = PasteSpreadsheetView.class)
class PasteSpreadsheetViewTest extends SpringBrowserlessTest {

    @Test
    void pastingHtmlTable_populatesGridFromOnPaste() {
        navigate(PasteSpreadsheetView.class);
        Div dropZone = findInView(Div.class).withClassName("drop-zone").single();

        // The drop zone reads the HTML branch of the clipboard (unambiguous for
        // spreadsheet ranges). Seed a small table and paste it.
        String tableHtml = "<table>"
                + "<tr><th>Region</th><th>Q1</th></tr>"
                + "<tr><td>North</td><td>100</td></tr>" + "</table>";
        ClipboardSimulator.current().setHtml(tableHtml);

        ClipboardSimulator.current().pasteInto(dropZone);

        // onPaste parsed the table (header + 1 data row) and reported it.
        assertEquals("Pasted 2 rows.", dropZone.getText());
    }
}
