package com.ecommerce.ecommerce.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a user's shipping address.
 */
@Embeddable
@Getter
@Setter
public class ShippingAddress {
    private String street;
    private String city;
    private String region;
    private String postalCode;
    private String country;
}