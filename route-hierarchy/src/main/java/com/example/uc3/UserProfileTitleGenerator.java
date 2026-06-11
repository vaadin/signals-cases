package com.example.uc3;

import com.vaadin.flow.router.PageTitleContext;
import com.vaadin.flow.router.PageTitleGenerator;

/**
 * Instance-free dynamic title for {@link UserProfileView}, introduced by
 * <a href="https://github.com/vaadin/flow/pull/24550">flow#24550</a>. It
 * resolves the person's name straight from the {@code :userId} route parameter,
 * so the breadcrumb leaf shows "Ada Lovelace" without the breadcrumb builder
 * ever instantiating the view — the gap UC3 used to demonstrate.
 */
public class UserProfileTitleGenerator implements PageTitleGenerator {

    @Override
    public String generatePageTitle(PageTitleContext context) {
        String userId = context.routeParameters().get("userId").orElse("?");
        return Directory.nameOf(userId);
    }
}
