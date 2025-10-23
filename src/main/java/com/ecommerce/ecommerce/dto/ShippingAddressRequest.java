package com.ecommerce.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for updating user shipping address.
 */

public class ShippingAddressRequest {

    @NotBlank(message = "Street is required.")
    @Size(max = 255)
    private String street;

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "Region is required.")
    private String region;

    @NotBlank(message = "Postal code is required.")
    private String postalCode;

    private String country = "Tanzania"; // Default value

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}