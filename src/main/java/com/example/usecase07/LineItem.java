package com.example.usecase07;

import java.math.BigDecimal;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

public class LineItem {
    private int id;
    private @Nullable String description;
    private int quantity;
    private @Nullable BigDecimal unitPrice;
    private @Nullable BigDecimal total;

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

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public @Nullable BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(@Nullable BigDecimal unitPrice) {
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public @Nullable BigDecimal getTotal() {
        return total;
    }

    public void setTotal(@Nullable BigDecimal total) {
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
