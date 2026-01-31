package com.amechato.ecommerce_api.backend.domain.ports;

import java.util.Optional;

import com.amechato.ecommerce_api.backend.domain.models.Category;

public interface ICategoryRepository {
    Category save(Category category);

    Iterable<Category> findAll();

    Optional<Category> findById(Integer id);

    void deleteById(Integer id);
}
