package com.example.usecase07;

import java.util.List;

import org.jspecify.annotations.Nullable;

public class InvoiceDetails {
    private @Nullable Invoice invoice;
    private @Nullable String customerEmail;
    private @Nullable String customerAddress;
    private @Nullable List<LineItem> lineItems;
    private @Nullable String paymentStatus;

    public InvoiceDetails() {
    }

    public InvoiceDetails(@Nullable Invoice invoice,
            @Nullable String customerEmail, @Nullable String customerAddress,
            @Nullable List<LineItem> lineItems,
            @Nullable String paymentStatus) {
        this.invoice = invoice;
        this.customerEmail = customerEmail;
        this.customerAddress = customerAddress;
        this.lineItems = lineItems;
        this.paymentStatus = paymentStatus;
    }

    public @Nullable Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(@Nullable Invoice invoice) {
        this.invoice = invoice;
    }

    public @Nullable String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(@Nullable String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public @Nullable String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(@Nullable String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public @Nullable List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(@Nullable List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public @Nullable String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(@Nullable String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public @Nullable LineItem getLineItemById(int id) {
        if (lineItems == null) {
            return null;
        }
        return lineItems.stream().filter(li -> li.getId() == id).findFirst()
                .orElse(null);
    }
}
