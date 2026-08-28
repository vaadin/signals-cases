package com.example.uc8;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.example.views.MainLayout;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.clipboard.Clipboard;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC8 — Copy a cell, a row, or the current selection out of a data grid.
 * <p>
 * This is the use case from the
 * <a href="https://vaadin.com/forum/t/clipboard-copy/164697/11">forum
 * thread</a>: a table where the user wants a copy affordance next to the cell
 * values, without "instantiating millions of components and instances of the
 * clipboard helper".
 * <p>
 * The obvious implementation — a {@code ComponentRenderer} column with a copy
 * {@code Button} per row, each with its own {@code Clipboard.onClick(...)}
 * binding — creates one component, one trigger and one client-side listener per
 * rendered row, all of them re-created as the user scrolls. This view avoids
 * that: the copy affordances are a single {@link GridContextMenu} and a single
 * toolbar {@link Button}, so the number of clipboard bindings is constant
 * (four) no matter how many rows the grid has.
 * <p>
 * The catch is that {@code Clipboard.onClick} needs to know <em>what</em> to
 * copy before the click happens: the value is either a literal fixed at binding
 * time or the {@code value} property of a component, read on the client inside
 * the click handler. Neither fits "whatever row the user just right-clicked".
 * The workaround here is to bind each action to an off-screen
 * {@link StagingSlot} that acts as a client-side staging slot, and to fill that
 * slot server-side just before the user can click:
 * <ul>
 * <li>for the context menu, from
 * {@link GridContextMenu#setDynamicContentHandler(com.vaadin.flow.function.SerializablePredicate)},
 * which runs a server round trip <em>before</em> the menu opens, so the staged
 * value has reached the browser by the time any menu item can be clicked;</li>
 * <li>for the toolbar button, from the grid's selection listener.</li>
 * </ul>
 * See {@code clipboard/API-GAPS.md} — this staging dance is the workaround for
 * a missing dynamic value source on the clipboard API.
 */
@Route(value = "uc8", layout = MainLayout.class)
@PageTitle("UC8 — Copy from a data grid")
@Menu(order = 8, title = "UC8 — Copy from a grid")
@StyleSheet("uc8.css")
public class CopyFromGridView extends VerticalLayout {

    /** A row of the demo grid. */
    public record Customer(String name, String company, String email,
            String phone) {
    }

    private static final String HEADER_ROW = "Name\tCompany\tEmail\tPhone";
    private static final int ROW_COUNT = 500;

    private final List<Customer> customers = generateCustomers(ROW_COUNT);

    // One staging slot per clipboard binding — not per row. The bound write
    // reads the slot's `value` property on the client at click time, which is
    // what makes a single binding able to copy a different value on every
    // click.
    final StagingSlot emailSlot = new StagingSlot();
    final StagingSlot phoneSlot = new StagingSlot();
    final StagingSlot rowSlot = new StagingSlot();
    final StagingSlot selectionSlot = new StagingSlot();

    final Grid<Customer> grid = new Grid<>();
    final Button copySelection = new Button("Copy selected rows",
            VaadinIcon.COPY.create());
    final GridContextMenu<Customer> contextMenu = grid.addContextMenu();

    public CopyFromGridView() {
        setSizeFull();
        add(new H1("UC8 — Copy from a data grid"));
        add(new Paragraph(
                "Right-click any row to copy a single cell value or the whole "
                        + "row, or tick a few rows and copy them as a table you "
                        + "can paste straight into a spreadsheet. The grid has "
                        + ROW_COUNT
                        + " rows but only four clipboard bindings — one per "
                        + "action, none per row."));

        grid.addColumn(Customer::name).setHeader("Name").setAutoWidth(true);
        grid.addColumn(Customer::company).setHeader("Company")
                .setAutoWidth(true);
        grid.addColumn(Customer::email).setHeader("Email").setAutoWidth(true);
        grid.addColumn(Customer::phone).setHeader("Phone").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.NO_BORDER);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setItems(customers);
        grid.setSizeFull();

        configureContextMenu();
        configureSelectionCopy();

        HorizontalLayout toolbar = new HorizontalLayout(copySelection);
        toolbar.setAlignItems(Alignment.CENTER);

        add(toolbar, grid);
        // The staging slots are off-screen but still part of the DOM: an
        // invisible (setVisible(false)) component would not have its value
        // pushed to the client until it became visible again, which would
        // break the copy.
        add(emailSlot, phoneSlot, rowSlot, selectionSlot);
    }

    private void configureContextMenu() {
        // The clipboard write has to be bound to the Span, not to the
        // GridMenuItem: unlike ContextMenu's MenuItem, GridMenuItem does not
        // implement ClickNotifier, so Clipboard.onClick(item) does not even
        // compile. The Span is stretched to fill the item so that clicking
        // anywhere on the row of the menu hits it. See API-GAPS.md.
        Span copyEmail = menuAction("Copy email");
        Span copyPhone = menuAction("Copy phone");
        Span copyRow = menuAction("Copy row");
        contextMenu.addItem(copyEmail);
        contextMenu.addItem(copyPhone);
        contextMenu.addItem(copyRow);

        Clipboard.onClick(copyEmail).writeText(emailSlot,
                copied -> Notification.show("Copied " + copied),
                error -> Notification.show("Copy failed: " + error.message()));
        Clipboard.onClick(copyPhone).writeText(phoneSlot,
                copied -> Notification.show("Copied " + copied),
                error -> Notification.show("Copy failed: " + error.message()));
        Clipboard.onClick(copyRow).writeText(rowSlot,
                copied -> Notification.show("Copied row"),
                error -> Notification.show("Copy failed: " + error.message()));

        // Runs on the server before the menu opens, so the staged values are
        // on the client by the time the user can click an item. This is also
        // the only reason the labels can name the target row.
        contextMenu.setDynamicContentHandler(customer -> {
            if (customer == null) {
                // Right-click on the header or on empty space below the rows.
                return false;
            }
            stageRow(customer);
            copyEmail.setText("Copy email — " + customer.email());
            copyPhone.setText("Copy phone — " + customer.phone());
            copyRow.setText("Copy row — " + customer.name());
            return true;
        });
    }

    private static Span menuAction(String label) {
        Span action = new Span(label);
        action.addClassName("menu-action");
        return action;
    }

    private void configureSelectionCopy() {
        copySelection.setEnabled(false);
        Clipboard.onClick(copySelection).writeText(selectionSlot,
                copied -> Notification.show("Copied "
                        + grid.getSelectedItems().size() + " rows as a table"),
                error -> Notification.show("Copy failed: " + error.message()));

        grid.addSelectionListener(
                event -> stageSelection(event.getAllSelectedItems()));
    }

    /** Stages the values the context menu can copy for the given row. */
    private void stageRow(Customer customer) {
        emailSlot.setValue(customer.email());
        phoneSlot.setValue(customer.phone());
        rowSlot.setValue(toTsv(List.of(customer)));
    }

    /**
     * Stages the current selection as a tab-separated table with a header row —
     * the format Excel, Numbers and Google Sheets split back into cells on
     * paste, and the same format UC5 parses on the way in.
     */
    private void stageSelection(Set<Customer> selected) {
        // getAllSelectedItems() is an unordered set; re-order by the grid's own
        // item order so the copied table matches what the user sees.
        List<Customer> ordered = customers.stream().filter(selected::contains)
                .toList();
        selectionSlot.setValue(ordered.isEmpty() ? "" : toTsv(ordered));
        copySelection.setEnabled(!ordered.isEmpty());
        copySelection.setText(ordered.isEmpty() ? "Copy selected rows"
                : "Copy " + ordered.size() + " selected rows");
    }

    private static String toTsv(List<Customer> rows) {
        return rows.stream()
                .map(c -> String.join("\t", c.name(), c.company(), c.email(),
                        c.phone()))
                .collect(Collectors.joining("\n", HEADER_ROW + "\n", ""));
    }

    /**
     * The smallest thing {@code Clipboard.onClick(...).writeText(...)} accepts
     * as a value source: a component with a String {@code value} property. It
     * is deliberately <em>not</em> an {@code <input>} — the HTML value
     * sanitisation algorithm strips newlines from an input's value, which
     * silently turns a multi-line copy (a whole row, or a table of selected
     * rows) into a single line. A {@code <span>} carrying a plain JS property
     * has no such rule. See API-GAPS.md.
     */
    @Tag(Tag.SPAN)
    static class StagingSlot
            extends AbstractSinglePropertyField<StagingSlot, String> {

        StagingSlot() {
            super("value", "", false);
            getElement().getClassList().add("clipboard-staging-slot");
            getElement().setAttribute("aria-hidden", "true");
        }
    }

    private static List<Customer> generateCustomers(int count) {
        String[] firstNames = { "Ada", "Grace", "Alan", "Linus", "Barbara",
                "Ken", "Margaret", "Dennis", "Radia", "Tim" };
        String[] lastNames = { "Lovelace", "Hopper", "Turing", "Torvalds",
                "Liskov", "Thompson", "Hamilton", "Ritchie", "Perlman",
                "Berners-Lee" };
        String[] companies = { "Northwind", "Contoso", "Initech", "Umbrella",
                "Globex", "Hooli", "Acme", "Soylent" };
        return IntStream.range(0, count).mapToObj(i -> {
            String first = firstNames[i % firstNames.length];
            String last = lastNames[(i / firstNames.length) % lastNames.length];
            String company = companies[i % companies.length];
            String name = first + " " + last + " " + (i + 1);
            String email = (first + "." + last + (i + 1) + "@"
                    + company.toLowerCase() + ".example").toLowerCase();
            String phone = String.format("+358 40 %03d %04d", i % 1000,
                    1000 + i);
            return new Customer(name, company, email, phone);
        }).toList();
    }
}
