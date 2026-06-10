package com.example.uc2;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Standard Spring Data repository. {@link #findAll()} issues a plain
 * {@code from Product} query; because {@link Product#getCategory()} is eager
 * and not batched, that one query fans out into a per-product join-table fetch
 * — the N+1 UC2 demonstrates.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
