package com.amechato.ecommerce_api.backend.application.usecases;

import com.amechato.ecommerce_api.backend.domain.models.Product;
import com.amechato.ecommerce_api.backend.domain.ports.IProductRepository;

public class ProductService {

    private final IProductRepository productRepository;

    public ProductService(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product save(Product product) {
        return this.productRepository.save(product);
    }

    public Iterable<Product> findAll() {
        return this.productRepository.findAll();
    }

    public Product findById(Integer id) {
        return this.productRepository.findById(id).orElse(null);
    }

    public void deleteById(Integer id) {
        this.productRepository.deleteById(id);
    }
}
