package com.example.uc2;

import java.util.HashSet;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a handful of products, each in one or two categories, so the join-table
 * fetch demo has something to load. Enough products that the N+1 fan-out is
 * unmistakable in the statement count, few enough to stay an in-memory toy.
 */
@Component
class ProductCatalogInitializer implements CommandLineRunner {

    private static final String[] CATEGORY_NAMES = { "Fiction", "Non-fiction",
            "Children", "Technical", "Reference" };
    private static final String[] PRODUCT_NAMES = { "The Pragmatic Programmer",
            "Clean Code", "Refactoring", "Effective Java",
            "Domain-Driven Design", "Working Effectively with Legacy Code",
            "The Mythical Man-Month", "Designing Data-Intensive Applications" };

    private final ProductRepository products;
    private final CategoryRepository categories;

    ProductCatalogInitializer(ProductRepository products,
            CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (products.count() > 0) {
            return;
        }

        Category[] saved = new Category[CATEGORY_NAMES.length];
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            saved[i] = categories.save(new Category(CATEGORY_NAMES[i]));
        }

        for (int i = 0; i < PRODUCT_NAMES.length; i++) {
            Product product = new Product(PRODUCT_NAMES[i]);
            // One or two categories per product — the actual count is
            // irrelevant; what matters is that every product owns a separate
            // collection that gets fetched in its own query.
            product.setCategory(new HashSet<>(List.of(saved[i % saved.length],
                    saved[(i + 1) % saved.length])));
            products.save(product);
        }
    }
}
