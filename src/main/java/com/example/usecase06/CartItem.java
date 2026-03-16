package com.example.usecase06;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record CartItem(Product product, int quantity) {

    public CartItem withQuantity(int newQuantity) {
        return new CartItem(product, newQuantity);
    }

    public BigDecimal totalPrice() {
        return product()
                .price().multiply(BigDecimal.valueOf(quantity()))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
