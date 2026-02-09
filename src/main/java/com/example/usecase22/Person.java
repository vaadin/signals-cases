package com.example.usecase22;

public record Person(String firstName, String lastName, String email, int age, Address address) {

    public Person withFirstName(String newFirstName) {
        return new Person(newFirstName, lastName, email, age, address);
    }

    public Person withLastName(String newLastName) {
        return new Person(firstName, newLastName, email, age, address);
    }

    public Person withEmail(String newEmail) {
        return new Person(firstName, lastName, newEmail, age, address);
    }

    public Person withAge(int newAge) {
        return new Person(firstName, lastName, email, newAge, address);
    }

    public Person withAddress(Address newAddress) {
        return new Person(firstName, lastName, email, age, newAddress);
    }

    public static Person empty() {
        return new Person("", "", "", 0, Address.empty());
    }
}
