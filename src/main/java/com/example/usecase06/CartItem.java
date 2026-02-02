package com.example.usecase06;

public record CartItem(Product product, int quantity) {

    public CartItem withQuantity(int newQuantity) {
        return new CartItem(product, newQuantity);
    }
}
