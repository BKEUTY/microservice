package com.bkeuty.order.exception;

public class CartItemNotFound extends RuntimeException {
    private Integer cartItemId;

    public CartItemNotFound(String message, Integer CartItemId) {

        super(message);
        this.cartItemId = CartItemId;
    }
}
