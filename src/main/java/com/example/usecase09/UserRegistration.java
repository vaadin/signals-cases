package com.example.usecase09;

import org.jspecify.annotations.Nullable;

public class UserRegistration {

    private @Nullable String username;
    private @Nullable String email;
    private @Nullable String password;
    private @Nullable String confirmPassword;
    private @Nullable AccountType accountType;
    private @Nullable Integer age;

    public UserRegistration() {
    }

    public UserRegistration(@Nullable String username, @Nullable String email,
            @Nullable String password, @Nullable String confirmPassword,
            @Nullable AccountType accountType, @Nullable Integer age) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.accountType = accountType;
        this.age = age;
    }

    public @Nullable String getUsername() {
        return username;
    }

    public @Nullable String getEmail() {
        return email;
    }

    public @Nullable String getPassword() {
        return password;
    }

    public @Nullable String getConfirmPassword() {
        return confirmPassword;
    }

    public @Nullable AccountType getAccountType() {
        return accountType;
    }

    public @Nullable Integer getAge() {
        return age;
    }

    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    public void setConfirmPassword(@Nullable String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setAccountType(@Nullable AccountType accountType) {
        this.accountType = accountType;
    }

    public void setAge(@Nullable Integer age) {
        this.age = age;
    }
}
