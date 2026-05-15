package com.example.uc4;

import java.util.ArrayList;
import java.util.List;

import com.example.views.MainLayout;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.vaadin.flow.component.clipboard.Clipboard;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC4 — Paste a table from a spreadsheet.
 * <p>
 * Select cells in the Vaadin Spreadsheet below and copy with Ctrl/Cmd-C, or
 * copy a range from Microsoft Excel or Google Sheets, then paste into the
 * drop zone. The clipboard carries the data as both plain text (tab
 * separated) and HTML (a {@code <table>}). The HTML branch is the only
 * unambiguous one when cell values contain tabs or newlines, so this view
 * parses {@code event.getHtml()} and hydrates a {@link Grid} from it.
 */
@Route(value = "uc4", layout = MainLayout.class)
@PageTitle("UC4 — Paste a table from a spreadsheet")
@Menu(order = 4, title = "UC4 — Paste a table")
@StyleSheet("uc4.css")
public class PasteSpreadsheetView extends VerticalLayout {

    public PasteSpreadsheetView() {
        addClassName("uc4-view");
        add(new H1("UC4 — Paste a table from a spreadsheet"));
        add(new Paragraph(
                "Select cells in the spreadsheet below and copy with Ctrl/Cmd-C, "
                        + "then paste into the drop zone. You can also copy a range "
                        + "from Microsoft Excel or Google Sheets — the clipboard's "
                        + "HTML branch is what makes cell boundaries unambiguous."));

        Spreadsheet spreadsheet = buildSampleSpreadsheet();
        add(spreadsheet);

        Div dropZone = new Div();
        dropZone.addClassName("drop-zone");
        dropZone.setText("Paste here (Ctrl+V / Cmd+V)");
        dropZone.setWidthFull();
        dropZone.getElement().setAttribute("tabindex", "0");

        Grid<List<String>> grid = new Grid<>();
        grid.setVisible(false);

        Clipboard.addPasteListener(dropZone, event -> {
            // Prefer HTML (Excel/Google Sheets always include a <table>;
            // unambiguous when cells contain tabs or newlines). Fall back to
            // tab-separated plain text, which is what the Vaadin Spreadsheet
            // puts on the clipboard.
            List<List<String>> rows = List.of();
            String html = event.getHtml();
            if (html != null) {
                rows = parseHtmlTable(html);
            }
            if (rows.isEmpty()) {
                String text = event.getText();
                if (text != null) {
                    rows = parseTsv(text);
                }
            }
            if (rows.isEmpty()) {
                dropZone.setText(
                        "No table on the clipboard — copy a range from "
                                + "the spreadsheet, Excel or Google Sheets.");
                return;
            }
            populateGrid(grid, rows);
            dropZone.setText("Pasted " + rows.size() + " rows.");
        });

        add(dropZone, grid);
    }

    private Spreadsheet buildSampleSpreadsheet() {
        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setHeight("260px");
        spreadsheet.setWidthFull();

        String[] headers = { "Region", "Q1", "Q2", "Q3", "Q4" };
        for (int col = 0; col < headers.length; col++) {
            spreadsheet.createCell(0, col, headers[col]);
        }
        Object[][] data = { { "North", 124000, 138500, 142300, 156800 },
                { "South", 98700, 102400, 119900, 131200 },
                { "East", 87600, 94500, 88700, 101300 },
                { "West", 145900, 152300, 148800, 167400 } };
        for (int r = 0; r < data.length; r++) {
            for (int c = 0; c < data[r].length; c++) {
                spreadsheet.createCell(r + 1, c, data[r][c]);
            }
        }
        return spreadsheet;
    }

    private List<List<String>> parseHtmlTable(String html) {
        Document doc = Jsoup.parse(html);
        Element table = doc.selectFirst("table");
        if (table == null) {
            return List.of();
        }
        List<List<String>> rows = new ArrayList<>();
        for (Element tr : table.select("tr")) {
            List<String> row = new ArrayList<>();
            for (Element cell : tr.select("th, td")) {
                row.add(cell.text());
            }
            if (!row.isEmpty()) {
                rows.add(row);
            }
        }
        return rows;
    }

    private List<List<String>> parseTsv(String text) {
        List<List<String>> rows = new ArrayList<>();
        for (String line : text.split("\r?\n")) {
            if (line.isEmpty()) {
                continue;
            }
            rows.add(List.of(line.split("\t", -1)));
        }
        return rows;
    }

    private void populateGrid(Grid<List<String>> grid, List<List<String>> rows) {
        grid.removeAllColumns();
        List<String> header = rows.getFirst();
        List<List<String>> body = rows.size() > 1 ? rows.subList(1, rows.size())
                : List.of();
        for (int i = 0; i < header.size(); i++) {
            int index = i;
            grid.addColumn(row -> index < row.size() ? row.get(index) : "")
                    .setHeader(header.get(i)).setAutoWidth(true);
        }
        grid.setItems(body);
        grid.setAllRowsVisible(true);
        grid.setVisible(true);
    }
}
