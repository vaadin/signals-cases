package com.example.uc2;

import com.vaadin.flow.router.PageTitleContext;
import com.vaadin.flow.router.PageTitleGenerator;

/**
 * Instance-free dynamic title for {@link OrderDetailView}: derives "Order
 * #&lt;id&gt;" from the {@code :orderId} route parameter so the breadcrumb leaf
 * reflects the actual order without a view instance
 * (<a href="https://github.com/vaadin/flow/pull/24550">flow#24550</a>).
 */
public class OrderTitleGenerator implements PageTitleGenerator {

    @Override
    public String generatePageTitle(PageTitleContext context) {
        String orderId = context.routeParameters().get("orderId").orElse("?");
        return "Order #" + orderId;
    }
}
