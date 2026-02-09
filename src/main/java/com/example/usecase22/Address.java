package com.example.usecase22;

public record Address(String street, String city, String zipCode, String country) {

    public Address withStreet(String newStreet) {
        return new Address(newStreet, city, zipCode, country);
    }

    public Address withCity(String newCity) {
        return new Address(street, newCity, zipCode, country);
    }

    public Address withZipCode(String newZipCode) {
        return new Address(street, city, newZipCode, country);
    }

    public Address withCountry(String newCountry) {
        return new Address(street, city, zipCode, newCountry);
    }

    public static Address empty() {
        return new Address("", "", "", "");
    }
}
