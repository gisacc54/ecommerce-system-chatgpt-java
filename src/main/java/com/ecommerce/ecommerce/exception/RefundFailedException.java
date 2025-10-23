// src/main/java/com/ecommerce/ecommerce/exception/RefundFailedException.java
package com.ecommerce.ecommerce.exception;

public class RefundFailedException extends RuntimeException {
    public RefundFailedException(String message) { super(message); }
}