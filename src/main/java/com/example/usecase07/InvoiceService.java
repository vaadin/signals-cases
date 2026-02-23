package com.example.usecase07;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    public static final Invoice EMPTY_INVOICE = new Invoice("", "",
            LocalDate.now(), BigDecimal.ZERO, "");

    public static final InvoiceDetails EMPTY_DETAILS = new InvoiceDetails(
            EMPTY_INVOICE, "", "", List.of(), "");

    public static final LineItem EMPTY_LINEITEM = new LineItem(-1, "", 0,
            BigDecimal.ZERO, BigDecimal.ZERO);

    private int invoiceIdCounter = 1;
    private int lineItemIdCounter = 1;

    private final List<Invoice> invoices = new ArrayList<>();
    private final List<InvoiceDetails> invoiceDetails = new ArrayList<>();
    {
        invoices.addAll(loadInitialInvoices());
        invoices.forEach(inv -> {
            String id = inv.getId();
            if (id != null) {
                InvoiceDetails details = loadInitialInvoiceDetails(id);
                if (details != null) {
                    invoiceDetails.add(details);
                }
            }
        });
    }

    private List<Invoice> loadInitialInvoices() {
        // Stub implementation - returns mock data
        return List.of(new Invoice("INV-" + invoiceIdCounter++, "Acme Corp",
                LocalDate.of(2025, 1, 15), new BigDecimal("1250.00"), "Paid"),
                new Invoice("INV-" + invoiceIdCounter++, "TechStart Inc",
                        LocalDate.of(2025, 1, 16), new BigDecimal("3400.50"),
                        "Pending"),
                new Invoice("INV-" + invoiceIdCounter++, "Global Solutions",
                        LocalDate.of(2025, 1, 17), new BigDecimal("890.00"),
                        "Paid"),
                new Invoice("INV-" + invoiceIdCounter++, "Beta Corp",
                        LocalDate.of(2025, 1, 18), new BigDecimal("2100.75"),
                        "Overdue"));
    }

    private @Nullable InvoiceDetails loadInitialInvoiceDetails(String invoiceId) {
        Invoice invoice = invoices.stream()
                .filter(inv -> invoiceId.equals(inv.getId())).findFirst()
                .orElseThrow();
        // Stub implementation - returns mock data
        return switch (invoiceId) {
        case "INV-1" -> new InvoiceDetails(invoice, "contact@acmecorp.com",
                "123 Main St, New York, NY 10001",
                List.of(new LineItem(lineItemIdCounter++, "Software License", 5,
                        new BigDecimal("200.00"), new BigDecimal("1000.00")),
                        new LineItem(lineItemIdCounter++, "Support Package", 1,
                                new BigDecimal("250.00"),
                                new BigDecimal("250.00"))),
                "Paid on 2025-01-20");
        case "INV-2" -> new InvoiceDetails(invoice, "info@techstart.com",
                "456 Tech Ave, San Francisco, CA 94102",
                List.of(new LineItem(lineItemIdCounter++, "Enterprise License",
                        10, new BigDecimal("300.00"),
                        new BigDecimal("3000.00")),
                        new LineItem(lineItemIdCounter++, "Training Session", 2,
                                new BigDecimal("200.00"),
                                new BigDecimal("400.00")),
                        new LineItem(lineItemIdCounter++, "Setup Fee", 1,
                                new BigDecimal("0.50"),
                                new BigDecimal("0.50"))),
                "Payment pending - Due 2025-02-15");
        case "INV-3" -> new InvoiceDetails(invoice,
                "billing@globalsolutions.com",
                "789 Business Blvd, Boston, MA 02108",
                List.of(new LineItem(lineItemIdCounter++, "Consulting Hours", 8,
                        new BigDecimal("100.00"), new BigDecimal("800.00")),
                        new LineItem(lineItemIdCounter++, "Travel Expenses", 1,
                                new BigDecimal("90.00"),
                                new BigDecimal("90.00"))),
                "Paid on 2025-01-25");
        case "INV-4" -> new InvoiceDetails(invoice, "accounts@betacorp.com",
                "321 Commerce St, Chicago, IL 60601",
                List.of(new LineItem(lineItemIdCounter++, "Annual Subscription",
                        1, new BigDecimal("2000.00"),
                        new BigDecimal("2000.00")),
                        new LineItem(lineItemIdCounter++, "Extra Storage", 1,
                                new BigDecimal("100.75"),
                                new BigDecimal("100.75"))),
                "OVERDUE - Payment was due 2025-01-31");
        default -> null;
        };
    }

    public Invoice createNewEmptyInvoice() {
        var invoice = new Invoice("INV-" + invoiceIdCounter++, "",
                LocalDate.now(), BigDecimal.ZERO, "");
        invoices.add(invoice);
        invoiceDetails.add(new InvoiceDetails(invoice, "", "", List.of(), ""));
        return invoice;
    }

    public LineItem createNewEmptyLineItem(String invoiceId) {
        LineItem lineItem = new LineItem(lineItemIdCounter++, "", 0,
                BigDecimal.ZERO, BigDecimal.ZERO);
        invoiceDetails.stream().filter(details -> {
            Invoice inv = details.getInvoice();
            return inv != null && invoiceId.equals(inv.getId());
        }).findFirst().ifPresent(details -> {
            List<LineItem> lineItems = details.getLineItems();
            List<LineItem> updatedLineItems = new ArrayList<>(
                    lineItems != null ? lineItems : List.of());
            updatedLineItems.add(lineItem);
            details.setLineItems(updatedLineItems);
            Invoice inv = details.getInvoice();
            if (inv != null) {
                updateInvoiceTotal(inv);
            }
        });
        return lineItem;
    }

    public List<Invoice> fetchInvoices() {
        return new ArrayList<>(invoices);
    }

    public InvoiceDetails fetchInvoiceDetails(String invoiceId) {
        return invoiceDetails.stream().filter(details -> {
            Invoice inv = details.getInvoice();
            return inv != null && invoiceId.equals(inv.getId());
        }).findFirst().orElseThrow();
    }

    public void updateInvoice(Invoice invoice) {
        invoices.replaceAll(inv -> {
            String invId = inv.getId();
            if (invId != null && invId.equals(invoice.getId())) {
                return invoice;
            }
            return inv;
        });
        invoiceDetails.replaceAll(details -> {
            Invoice detInv = details.getInvoice();
            String detInvId = detInv != null ? detInv.getId() : null;
            if (detInvId != null && detInvId.equals(invoice.getId())) {
                return new InvoiceDetails(invoice, details.getCustomerEmail(),
                        details.getCustomerAddress(), details.getLineItems(),
                        details.getPaymentStatus());
            }
            return details;
        });
    }

    public void updateDetails(InvoiceDetails details) {
        invoiceDetails.replaceAll(oldDetails -> {
            Invoice oldInv = oldDetails.getInvoice();
            Invoice newInv = details.getInvoice();
            String oldId = oldInv != null ? oldInv.getId() : null;
            String newId = newInv != null ? newInv.getId() : null;
            if (oldId != null && oldId.equals(newId)) {
                return details;
            }
            return oldDetails;
        });
    }

    public InvoiceDetails updateLineItem(Invoice invoice, LineItem lineItem) {
        invoiceDetails.replaceAll(oldDetails -> {
            Invoice oldInv = oldDetails.getInvoice();
            String oldId = oldInv != null ? oldInv.getId() : null;
            if (oldId != null && oldId.equals(invoice.getId())) {
                List<LineItem> oldLineItems = oldDetails.getLineItems();
                List<LineItem> lineItems = new ArrayList<>(
                        oldLineItems != null ? oldLineItems : List.of());
                lineItems.replaceAll(li -> {
                    if (li.getId() == lineItem.getId()) {
                        BigDecimal unitPrice = lineItem.getUnitPrice();
                        if (unitPrice == null) {
                            unitPrice = BigDecimal.ZERO;
                        }
                        lineItem.setTotal(unitPrice.multiply(
                                BigDecimal.valueOf(lineItem.getQuantity())));
                        return lineItem;
                    }
                    return li;
                });
                oldDetails
                        .setLineItems(Collections.unmodifiableList(lineItems));
                return oldDetails;
            }
            return oldDetails;
        });
        // recalculate invoice total based on Line items.
        updateInvoiceTotal(invoice);

        String invoiceId = invoice.getId();
        if (invoiceId == null) {
            invoiceId = "";
        }
        return fetchInvoiceDetails(invoiceId);
    }

    private void updateInvoiceTotal(Invoice invoice) {
        BigDecimal newTotal = invoiceDetails.stream()
                .filter(details -> {
                    Invoice inv = details.getInvoice();
                    return inv != null && invoice.getId() != null
                            && invoice.getId().equals(inv.getId());
                })
                .findFirst().map(InvoiceDetails::getLineItems).orElse(List.of())
                .stream().map(li -> {
                    BigDecimal total = li.getTotal();
                    return total != null ? total : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setTotal(newTotal);
        updateInvoice(invoice);
    }

    public void removeLineItem(String id, LineItem lineItem) {
        InvoiceDetails details = fetchInvoiceDetails(id);
        List<LineItem> existingItems = details.getLineItems();
        List<LineItem> lineItems = new ArrayList<>(
                existingItems != null ? existingItems : List.of());
        lineItems.removeIf(li -> li.getId() == lineItem.getId());
        details.setLineItems(Collections.unmodifiableList(lineItems));
        Invoice inv = details.getInvoice();
        if (inv != null) {
            updateInvoiceTotal(inv);
        }
    }

    public Invoice fetchInvoice(String id) {
        return invoices.stream().filter(inv -> id.equals(inv.getId()))
                .findFirst().orElseThrow();
    }
}
