package com.example.usecase09;

record UserRegistration(String username, String email, String password,
        String confirmPassword, AccountType accountType, Integer age) {
}
