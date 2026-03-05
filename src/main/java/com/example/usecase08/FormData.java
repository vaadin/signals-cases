package com.example.usecase08;

import org.jspecify.annotations.Nullable;

/**
 * Mutable data class for wizard form binding with Binder.
 */
public class FormData {
    private String firstName = "";
    private String lastName = "";
    private String email = "";
    private String companyName = "";
    private String companySize = "";
    private String industry = "";
    private @Nullable Plan plan = Plan.STARTER;

    public FormData() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanySize() {
        return companySize;
    }

    public void setCompanySize(String companySize) {
        this.companySize = companySize;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public @Nullable Plan getPlan() {
        return plan;
    }

    public void setPlan(@Nullable Plan plan) {
        this.plan = plan;
    }
}
