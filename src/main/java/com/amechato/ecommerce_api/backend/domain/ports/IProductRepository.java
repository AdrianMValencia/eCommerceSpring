package com.amechato.ecommerce_api.backend.domain.ports;

import java.util.Optional;

import com.amechato.ecommerce_api.backend.domain.models.Product;

public interface IProductRepository {
    Product save(Product product);
    Iterable<Product> findAll();
    Optional<Product> findById(Integer id);
    void deleteById(Integer id);
}
