package com.example.uc2;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads the product catalog. The load deliberately fans out into the N+1
 * join-table fetch (see {@link Product}); how that fan-out is <em>observed</em>
 * is not this service's concern — it issues the queries, and the Observability
 * Kit's database feature ({@code vaadin.observability.database=true}) records
 * each JDBC result-set fetch into the {@code vaadin.db.fetch.rows} summary and
 * a {@code vaadin.db.query} span, with no code here touching the driver.
 */
@Service
public class ProductCatalogService {

    /**
     * What one catalog load returned (the cost is read from the kit's meters).
     */
    public record CatalogLoad(int products, int categories) {
    }

    private final ProductRepository products;

    public ProductCatalogService(ProductRepository products) {
        this.products = products;
    }

    /**
     * Loads every product and touches its categories so the eager join-table
     * fetch actually runs. Runs in a single transaction so the whole fan-out
     * happens as one unit of work — and, because the {@code category}
     * association is eager and unbatched, as one query per product.
     */
    @Transactional(readOnly = true)
    public CatalogLoad loadCatalog() {
        List<Product> all = products.findAll();
        int categories = 0;
        for (Product product : all) {
            // Touch the collection; with an eager, unbatched association the
            // per-product join-table query has already run during findAll,
            // which is what the kit's fetch meter counts.
            categories += product.getCategory().size();
        }
        return new CatalogLoad(all.size(), categories);
    }
}
