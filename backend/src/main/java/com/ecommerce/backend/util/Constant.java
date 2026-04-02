package com.ecommerce.backend.util;

public class Constant {

    private Constant() {
        throw new IllegalStateException("Utility class");
    }

    // USER
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_ALREADY_EXISTS = "User already exists with this email";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String UNAUTHORIZED = "Unauthorized access";

    // PRODUCT
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String PRODUCT_OUT_OF_STOCK = "Product is out of stock";

    // CART
    public static final String CART_NOT_FOUND = "Cart not found";
    public static final String CART_ITEM_NOT_FOUND = "Cart item not found";
    public static final String CART_ITEM_EMPTY = "Cart item is empty";
    public static final String QUANTITY_EXCEEDS_STOCK = "Requested quantity exceeds stock";
    public static final String CART_EMPTY = "Cart is empty";

    // ORDER
    public static final String ORDER_NOT_FOUND = "Order not found";
    public static final String ORDER_ALREADY_PAID = "Order is already paid and cannot be modified";
    public static final String ORDER_CANNOT_BE_UPDATED = "Order cannot be updated";
    public static final String ORDER_ACCESS_DENIED = "You are not authorized to access this order";
    public static final String ORDER_STOCK_CHANGED = "Stock changed, please update your cart";

    // PAYMENT
    public static final String PAYMENT_METHOD_NOT_SUPPORTED = "Only STRIPE payment method is supported";
    public static final String STRIPE_KEY_NOT_CONFIGURED = "Stripe secret key is not configured";
}
