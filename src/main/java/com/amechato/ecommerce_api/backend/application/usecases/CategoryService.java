package com.amechato.ecommerce_api.backend.application.usecases;

import com.amechato.ecommerce_api.backend.domain.models.Category;
import com.amechato.ecommerce_api.backend.domain.ports.ICategoryRepository;

public class CategoryService {
    private final ICategoryRepository categoryRepository;

    public CategoryService(ICategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Iterable<Category> findAll() {
        return this.categoryRepository.findAll();
    }

    public Category findById(Integer id) {
        return this.categoryRepository.findById(id).orElse(null);
    }

    public Category save(Category category) {
        return this.categoryRepository.save(category);
    }

    public void deleteById(Integer id) {
        this.categoryRepository.deleteById(id);
    }
}
