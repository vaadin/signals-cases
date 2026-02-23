package com.example.usecase07;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

public class Invoice {
    private @Nullable String id;
    private @Nullable String customerName;
    private @Nullable LocalDate dueDate;
    private @Nullable BigDecimal total;
    private @Nullable String status;

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

    public @Nullable String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public @Nullable String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(@Nullable String customerName) {
        this.customerName = customerName;
    }

    public @Nullable LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(@Nullable LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public @Nullable BigDecimal getTotal() {
        return total;
    }

    public void setTotal(@Nullable BigDecimal total) {
        this.total = total;
    }

    public @Nullable String getStatus() {
        return status;
    }

    public void setStatus(@Nullable String status) {
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
