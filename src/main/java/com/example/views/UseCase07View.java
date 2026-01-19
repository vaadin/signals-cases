package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEffect;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.ListSignal;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

import static com.example.views.UseCase07View.InvoiceService.EMPTY_DETAILS;
import static com.example.views.UseCase07View.InvoiceService.EMPTY_INVOICE;

@Route(value = "use-case-7", layout = MainLayout.class)
@PageTitle("Use Case 7: Master-Detail Invoice View")
@Menu(order = 7, title = "UC 7: Master-Detail Invoice")
@PermitAll
public class UseCase07View extends VerticalLayout {

    private final InvoiceService invoiceService = new InvoiceService();

    private final ListSignal<Invoice> invoiceListSignal = new ListSignal<>(
            Invoice.class);

    private final ListSignal<LineItem> lineItemsSignal = new ListSignal<>(
            LineItem.class);

    private Grid<Invoice> invoiceGrid;

    public UseCase07View() {

        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 7: Master-Detail Invoice View");

        Paragraph description = new Paragraph(
                "This use case demonstrates a master-detail pattern with reactive selection handling. "
                        + "Select an invoice from the grid to see its detailed information in the side panel. "
                        + "The detail panel updates automatically when you select a different invoice, "
                        + "showing customer information, line items, and payment status.");

        invoiceService.fetchInvoices().forEach(invoiceListSignal::insertLast);

        // Create signal for selected invoice (use empty invoice as initial
        // value)
        WritableSignal<Invoice> selectedInvoiceSignal = new ValueSignal<>(
                EMPTY_INVOICE);

        WritableSignal<LineItem> selectedLineItemSignal = new ValueSignal<>(
                InvoiceService.EMPTY_LINEITEM);

        // Computed signal for invoice details
        Signal<InvoiceDetails> invoiceDetailsSignal = Signal.computed(() -> {
            Invoice selected = selectedInvoiceSignal.value();
            return (selected != null && !selected.getId().isEmpty())
                    ? invoiceService.fetchInvoiceDetails(selected.getId())
                    : EMPTY_DETAILS;
        });

        // Master: Invoice grid
        invoiceGrid = new Grid<>(Invoice.class);
        invoiceGrid.setColumns("id", "customerName", "dueDate", "total",
                "status");
        ComponentEffect.bind(invoiceGrid, invoiceListSignal, (grid, items) -> {
            var invoices = invoiceListSignal.value().stream()
                    .map(ValueSignal::peek).toList();
            if (items != null) {
                grid.setItems(invoices);
            } else {
                grid.setItems(List.of());
            }
            invoiceListSignal.value().forEach(signal -> ComponentEffect
                    .bind(invoiceGrid, signal, (g, inv) -> {
                        g.getDataProvider().refreshItem(inv);
                    }));
        });
        invoiceGrid.asSingleSelect().addValueChangeListener(e -> {
            Invoice selected = e.getValue();
            selectedInvoiceSignal
                    .value(selected != null ? selected : EMPTY_INVOICE);
        });

        // Detail: Invoice details panel
        VerticalLayout detailsPanel = new VerticalLayout();
        detailsPanel.add(new H3("Invoice Details"));

        // Customer information
        FormLayout customerInfo = new FormLayout();
        customerInfo.setWidthFull();
        TextField customerName = new TextField();
        customerInfo.addFormItem(customerName, "Customer:");

        EmailField customerEmail = new EmailField();
        customerEmail.setHelperText("Valid email address required");
        customerInfo.addFormItem(customerEmail, "Email:");

        TextField customerAddress = new TextField();
        customerInfo.addFormItem(customerAddress, "Address:");

        DatePicker dueDate = new DatePicker();
        customerInfo.addFormItem(dueDate, "Due Date:");

        Binder<Invoice> invoiceBinder = new Binder<>(Invoice.class);
        invoiceBinder.forField(customerName).bind(Invoice::getCustomerName,
                Invoice::setCustomerName);
        invoiceBinder.forField(dueDate).bind(Invoice::getDueDate,
                Invoice::setDueDate);
        ComponentEffect.effect(this, () -> invoiceBinder
                .setBean(invoiceDetailsSignal.value().getInvoice()));
        invoiceBinder.addValueChangeListener(event -> {
            var status = invoiceBinder.validate();
            if (status.isOk()) {
                Invoice invoice = invoiceBinder.getBean();
                invoiceService.updateInvoice(invoice);
                // update invoiceListSignal's matching ValueSignal value for the
                // changed invoice
                updateInvoiceListSignalItem(invoice);
            } else if (status.hasErrors()) {
                Notification.show("Invoice has validation errors.");
            }
        });

        Binder<InvoiceDetails> detailsBinder = new Binder<>(
                InvoiceDetails.class);
        detailsBinder.forField(customerEmail)
                .withValidator(new EmailValidator("Invalid email address"))
                .bind(InvoiceDetails::getCustomerEmail,
                        InvoiceDetails::setCustomerEmail);
        detailsBinder.forField(customerAddress).bind(
                InvoiceDetails::getCustomerAddress,
                InvoiceDetails::setCustomerAddress);
        ComponentEffect.effect(this,
                () -> detailsBinder.setBean(invoiceDetailsSignal.value()));
        detailsBinder.addValueChangeListener(event -> {
            var status = detailsBinder.validate();
            if (status.isOk()) {
                InvoiceDetails details = detailsBinder.getBean();
                invoiceService.updateDetails(details);
            } else if (status.hasErrors()) {
                Notification.show("Invoice details has validation errors.");
            }
        });

        // Line items grid
        Grid<LineItem> lineItemsGrid = new Grid<>(LineItem.class);
        lineItemsGrid.setHeightFull();
        lineItemsGrid.setAllRowsVisible(true);
        lineItemsGrid.setSelectionMode(Grid.SelectionMode.NONE);
        lineItemsGrid.setColumns("description", "quantity", "unitPrice",
                "total");
        lineItemsGrid.appendFooterRow();
        lineItemsGrid.addComponentColumn(lineItem -> {
            Button button = new Button();
            button.setTooltipText("Remove Line Item");
            button.addClickListener(e -> {
                removeLineItem(invoiceDetailsSignal.value(), lineItem);
            });
            button.setIcon(VaadinIcon.CLOSE.create());
            return button;
        });

        ComponentEffect.bind(lineItemsGrid, lineItemsSignal, (grid, items) -> {
            var lineItems = lineItemsSignal.value().stream()
                    .map(ValueSignal::peek).toList();
            if (items != null) {
                grid.setItems(lineItems);
            } else {
                grid.setItems(List.of());
            }
            updateFooterTotal(lineItemsGrid);
            lineItemsSignal.value().forEach(signal -> ComponentEffect
                    .bind(lineItemsGrid, signal, (g, inv) -> {
                        // refresh the item in the grid
                        g.getDataProvider().refreshItem(inv);
                        // update footer total
                        updateFooterTotal(lineItemsGrid);
                    }));
        });

        ComponentEffect.effect(lineItemsGrid, () -> {
            List<LineItem> items = invoiceDetailsSignal.value().getLineItems();
            lineItemsSignal.clear();
            items.forEach(lineItemsSignal::insertLast);
        });

        // Line item editor fields
        TextField descriptionField = new TextField();
        descriptionField.setSizeFull();
        descriptionField.setThemeVariants(TextFieldVariant.LUMO_SMALL);

        IntegerField quantityField = new IntegerField();
        quantityField.setSizeFull();
        quantityField.setThemeVariants(TextFieldVariant.LUMO_SMALL);

        BigDecimalField unitPriceField = new BigDecimalField();
        unitPriceField.setSizeFull();
        unitPriceField.setThemeVariants(TextFieldVariant.LUMO_SMALL);

        // Line item editor binder (unbuffered)
        Binder<LineItem> lineItemBinder = new Binder<>(LineItem.class);
        lineItemBinder.forField(descriptionField).bind(LineItem::getDescription,
                LineItem::setDescription);
        lineItemBinder.forField(quantityField).bind(LineItem::getQuantity,
                LineItem::setQuantity);
        lineItemBinder.forField(unitPriceField).bind(LineItem::getUnitPrice,
                LineItem::setUnitPrice);

        ComponentEffect.effect(this,
                () -> lineItemBinder.setBean(selectedLineItemSignal.value()));
        lineItemBinder.addValueChangeListener(event -> {
            var status = lineItemBinder.validate();
            if (status.isOk()) {
                LineItem lineItem = lineItemBinder.getBean();
                InvoiceDetails details = invoiceService.updateLineItem(
                        detailsBinder.getBean().getInvoice(), lineItem);
                // update invoiceListSignal's matching ValueSignal value for the
                // changed invoice
                updateInvoiceListSignalItem(details.getInvoice());
                // update lineItemsSignal's matching ValueSignal value for the
                // changed line item
                lineItemsSignal.peek().stream().filter(
                        signal -> signal.peek().getId() == lineItem.getId())
                        .findFirst().ifPresent(signal -> {
                            signal.value(
                                    details.getLineItemById(lineItem.getId()));
                        });
            } else if (status.hasErrors()) {
                Notification.show("Line item has validation errors.");
            }
        });

        lineItemsGrid.getEditor().setBinder(lineItemBinder);
        lineItemsGrid.getColumnByKey("description")
                .setEditorComponent(descriptionField);
        lineItemsGrid.getColumnByKey("quantity")
                .setEditorComponent(quantityField);
        lineItemsGrid.getColumnByKey("unitPrice")
                .setEditorComponent(unitPriceField);
        lineItemsGrid.addItemDoubleClickListener(e -> {
            lineItemsGrid.getEditor().editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable<?> focusable) {
                focusable.focus();
            }
        });

        // Payment status
        Span paymentStatus = new Span();
        paymentStatus.bindText(invoiceDetailsSignal.map(
                details -> "Payment Status: " + details.getPaymentStatus()));
        paymentStatus.getStyle().set("font-weight", "bold");

        detailsPanel.add(customerInfo, new H3("Line Items"),
                new Span("(Double-click a cell to edit)"));
        detailsPanel.add(new Button("Add Line Item",
                e -> addNewLineItem(invoiceDetailsSignal.value())));
        detailsPanel.addAndExpand(lineItemsGrid);
        detailsPanel.add(paymentStatus);
        detailsPanel
                .bindVisible(invoiceDetailsSignal.map(details -> details != null
                        && details.getInvoice().getId() != null
                        && details != EMPTY_DETAILS));

        Button addInvoiceButton = new Button("New Invoice",
                e -> addNewInvoice());

        // Layout
        VerticalLayout mainLayout = new VerticalLayout();
        HorizontalLayout contentLayout = new HorizontalLayout(invoiceGrid,
                detailsPanel);
        contentLayout.setWidthFull();
        mainLayout.add(addInvoiceButton);
        mainLayout.addAndExpand(contentLayout);
        invoiceGrid.setWidth("60%");
        detailsPanel.setWidth("50%");
        detailsPanel.setHeightFull();
        add(title, description, mainLayout);
    }

    private void updateInvoiceListSignalItem(Invoice invoice) {
        invoiceListSignal.peek().stream()
                .filter(signal -> signal.peek().getId().equals(invoice.getId()))
                .findFirst().ifPresent(signal -> {
                    signal.value(invoice);
                });
    }

    private void updateFooterTotal(Grid<LineItem> lineItemsGrid) {
        lineItemsGrid.getColumnByKey("total")
                .setFooter("Total: "
                        + lineItemsSignal.peek().stream().map(ValueSignal::peek)
                                .toList().stream().map(LineItem::getTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private void addNewInvoice() {
        Invoice invoice = invoiceService.createNewEmptyInvoice();
        invoiceListSignal.insertLast(invoice);
        invoiceGrid.select(invoice);
    }

    private void addNewLineItem(InvoiceDetails details) {
        LineItem item = invoiceService
                .createNewEmptyLineItem(details.getInvoice().getId());
        lineItemsSignal.insertLast(item);
    }

    private void removeLineItem(InvoiceDetails value, LineItem lineItem) {
        invoiceService.removeLineItem(value.getInvoice().getId(), lineItem);
        lineItemsSignal.value().stream().filter(v -> v.peek().equals(lineItem))
                .findFirst().ifPresent(lineItemsSignal::remove);
        // need to also update the invoice total in the invoice list
        updateInvoiceListSignalItem(
                invoiceService.fetchInvoice(value.getInvoice().getId()));
    }

    static class InvoiceService {

        static final Invoice EMPTY_INVOICE = new Invoice("", "",
                LocalDate.now(), BigDecimal.ZERO, "");

        static final InvoiceDetails EMPTY_DETAILS = new InvoiceDetails(
                EMPTY_INVOICE, "", "", List.of(), "");

        static final LineItem EMPTY_LINEITEM = new LineItem(-1, "", 0,
                BigDecimal.ZERO, BigDecimal.ZERO);

        private int invoiceIdCounter = 1;
        private int lineItemIdCounter = 1;

        private final List<Invoice> invoices = new ArrayList<>();
        private final List<InvoiceDetails> invoiceDetails = new ArrayList<>();
        {
            invoices.addAll(loadInitialInvoices());
            invoices.forEach(inv -> invoiceDetails
                    .add(loadInitialInvoiceDetails(inv.getId())));
        }

        private List<Invoice> loadInitialInvoices() {
            // Stub implementation - returns mock data
            return List.of(
                    new Invoice("INV-" + invoiceIdCounter++, "Acme Corp",
                            LocalDate.of(2025, 1, 15),
                            new BigDecimal("1250.00"), "Paid"),
                    new Invoice("INV-" + invoiceIdCounter++, "TechStart Inc",
                            LocalDate.of(2025, 1, 16),
                            new BigDecimal("3400.50"), "Pending"),
                    new Invoice("INV-" + invoiceIdCounter++, "Global Solutions",
                            LocalDate.of(2025, 1, 17), new BigDecimal("890.00"),
                            "Paid"),
                    new Invoice("INV-" + invoiceIdCounter++, "Beta Corp",
                            LocalDate.of(2025, 1, 18),
                            new BigDecimal("2100.75"), "Overdue"));
        }

        private InvoiceDetails loadInitialInvoiceDetails(String invoiceId) {
            Invoice invoice = invoices.stream()
                    .filter(inv -> invoiceId.equals(inv.getId())).findFirst()
                    .orElseThrow();
            // Stub implementation - returns mock data
            return switch (invoiceId) {
            case "INV-1" -> new InvoiceDetails(invoice, "contact@acmecorp.com",
                    "123 Main St, New York, NY 10001",
                    List.of(new LineItem(lineItemIdCounter++,
                            "Software License", 5, new BigDecimal("200.00"),
                            new BigDecimal("1000.00")),
                            new LineItem(lineItemIdCounter++, "Support Package",
                                    1, new BigDecimal("250.00"),
                                    new BigDecimal("250.00"))),
                    "Paid on 2025-01-20");
            case "INV-2" -> new InvoiceDetails(invoice, "info@techstart.com",
                    "456 Tech Ave, San Francisco, CA 94102",
                    List.of(new LineItem(lineItemIdCounter++,
                            "Enterprise License", 10, new BigDecimal("300.00"),
                            new BigDecimal("3000.00")),
                            new LineItem(lineItemIdCounter++,
                                    "Training Session", 2,
                                    new BigDecimal("200.00"),
                                    new BigDecimal("400.00")),
                            new LineItem(lineItemIdCounter++, "Setup Fee", 1,
                                    new BigDecimal("0.50"),
                                    new BigDecimal("0.50"))),
                    "Payment pending - Due 2025-02-15");
            case "INV-3" -> new InvoiceDetails(invoice,
                    "billing@globalsolutions.com",
                    "789 Business Blvd, Boston, MA 02108",
                    List.of(new LineItem(lineItemIdCounter++,
                            "Consulting Hours", 8, new BigDecimal("100.00"),
                            new BigDecimal("800.00")),
                            new LineItem(lineItemIdCounter++, "Travel Expenses",
                                    1, new BigDecimal("90.00"),
                                    new BigDecimal("90.00"))),
                    "Paid on 2025-01-25");
            case "INV-4" -> new InvoiceDetails(invoice, "accounts@betacorp.com",
                    "321 Commerce St, Chicago, IL 60601",
                    List.of(new LineItem(lineItemIdCounter++,
                            "Annual Subscription", 1, new BigDecimal("2000.00"),
                            new BigDecimal("2000.00")),
                            new LineItem(lineItemIdCounter++, "Extra Storage",
                                    1, new BigDecimal("100.75"),
                                    new BigDecimal("100.75"))),
                    "OVERDUE - Payment was due 2025-01-31");
            default -> null;
            };
        }

        public Invoice createNewEmptyInvoice() {
            var invoice = new Invoice("INV-" + invoiceIdCounter++, "",
                    LocalDate.now(), BigDecimal.ZERO, "");
            invoices.add(invoice);
            invoiceDetails
                    .add(new InvoiceDetails(invoice, "", "", List.of(), ""));
            return invoice;
        }

        public LineItem createNewEmptyLineItem(String invoiceId) {
            LineItem lineItem = new LineItem(lineItemIdCounter++, "", 0,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            invoiceDetails.stream().filter(
                    details -> details.getInvoice().getId().equals(invoiceId))
                    .findFirst().ifPresent(details -> {
                        List<LineItem> updatedLineItems = new ArrayList<>(
                                details.getLineItems());
                        updatedLineItems.add(lineItem);
                        details.setLineItems(updatedLineItems);
                        updateInvoiceTotal(details.getInvoice());
                    });
            return lineItem;
        }

        public List<Invoice> fetchInvoices() {
            return new ArrayList<>(invoices);
        }

        public InvoiceDetails fetchInvoiceDetails(String invoiceId) {
            return invoiceDetails.stream().filter(
                    details -> details.getInvoice().getId().equals(invoiceId))
                    .findFirst().orElseThrow();
        }

        public void updateInvoice(Invoice invoice) {
            invoices.replaceAll(inv -> {
                if (inv.getId().equals(invoice.getId())) {
                    return invoice;
                }
                return inv;
            });
            invoiceDetails.replaceAll(details -> {
                if (details.getInvoice().getId().equals(invoice.getId())) {
                    return new InvoiceDetails(invoice,
                            details.getCustomerEmail(),
                            details.getCustomerAddress(),
                            details.getLineItems(), details.getPaymentStatus());
                }
                return details;
            });
        }

        public void updateDetails(InvoiceDetails details) {
            invoiceDetails.replaceAll(oldDetails -> {
                if (oldDetails.getInvoice().getId()
                        .equals(details.getInvoice().getId())) {
                    return details;
                }
                return oldDetails;
            });
        }

        public InvoiceDetails updateLineItem(Invoice invoice,
                LineItem lineItem) {
            invoiceDetails.replaceAll(oldDetails -> {
                if (oldDetails.getInvoice().getId().equals(invoice.getId())) {
                    List<LineItem> lineItems = new ArrayList<>(
                            oldDetails.getLineItems());
                    lineItems.replaceAll(li -> {
                        if (li.getId() == lineItem.getId()) {
                            lineItem.setTotal(
                                    lineItem.getUnitPrice().multiply(BigDecimal
                                            .valueOf(lineItem.getQuantity())));
                            return lineItem;
                        }
                        return li;
                    });
                    oldDetails.setLineItems(
                            Collections.unmodifiableList(lineItems));
                    return oldDetails;
                }
                return oldDetails;
            });
            // recalculate invoice total based on Line items.
            updateInvoiceTotal(invoice);

            return fetchInvoiceDetails(invoice.getId());
        }

        private void updateInvoiceTotal(Invoice invoice) {
            BigDecimal newTotal = invoiceDetails.stream()
                    .filter(details -> details.getInvoice().getId()
                            .equals(invoice.getId()))
                    .findFirst().map(InvoiceDetails::getLineItems)
                    .orElse(List.of()).stream().map(LineItem::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            invoice.setTotal(newTotal);
            updateInvoice(invoice);
        }

        public void removeLineItem(String id, LineItem lineItem) {
            InvoiceDetails details = fetchInvoiceDetails(id);
            List<LineItem> lineItems = new ArrayList<>(details.getLineItems());
            lineItems.removeIf(li -> li.getId() == lineItem.getId());
            details.setLineItems(Collections.unmodifiableList(lineItems));
            updateInvoiceTotal(details.getInvoice());
        }

        public Invoice fetchInvoice(String id) {
            return invoices.stream().filter(inv -> inv.getId().equals(id))
                    .findFirst().orElseThrow();
        }
    }

    public static class Invoice {
        private String id;
        private String customerName;
        private LocalDate dueDate;
        private BigDecimal total;
        private String status;

        public Invoice() {
        }

        public Invoice(String id, String customerName, LocalDate dueDate,
                BigDecimal total, String status) {
            this.id = id;
            this.customerName = customerName;
            this.dueDate = dueDate;
            this.total = total;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass())
                return false;
            Invoice invoice = (Invoice) o;
            return Objects.equals(getId(), invoice.getId());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getId());
        }
    }

    public static class LineItem {
        private int id;
        private String description;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal total;

        public LineItem() {
        }

        public LineItem(int id, String description, int quantity,
                BigDecimal unitPrice, BigDecimal total) {
            this.id = id;
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.total = total;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total != null ? total : BigDecimal.ZERO;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass())
                return false;
            LineItem lineItem = (LineItem) o;
            return getId() == lineItem.getId();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getId());
        }
    }

    public static class InvoiceDetails {
        private Invoice invoice;
        private String customerEmail;
        private String customerAddress;
        private List<LineItem> lineItems;
        private String paymentStatus;

        public InvoiceDetails() {
        }

        public InvoiceDetails(Invoice invoice, String customerEmail,
                String customerAddress, List<LineItem> lineItems,
                String paymentStatus) {
            this.invoice = invoice;
            this.customerEmail = customerEmail;
            this.customerAddress = customerAddress;
            this.lineItems = lineItems;
            this.paymentStatus = paymentStatus;
        }

        public Invoice getInvoice() {
            return invoice;
        }

        public void setInvoice(Invoice invoice) {
            this.invoice = invoice;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

        public String getCustomerAddress() {
            return customerAddress;
        }

        public void setCustomerAddress(String customerAddress) {
            this.customerAddress = customerAddress;
        }

        public List<LineItem> getLineItems() {
            return lineItems;
        }

        public void setLineItems(List<LineItem> lineItems) {
            this.lineItems = lineItems;
        }

        public String getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public LineItem getLineItemById(int id) {
            return lineItems.stream().filter(li -> li.getId() == id).findFirst()
                    .orElse(null);
        }
    }
}
