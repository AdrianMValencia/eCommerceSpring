package com.amechato.ecommerce_api.backend.infrastructure.adapters;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.amechato.ecommerce_api.backend.domain.models.Category;
import com.amechato.ecommerce_api.backend.domain.ports.ICategoryRepository;
import com.amechato.ecommerce_api.backend.infrastructure.mappers.CategoryMapper;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CategoryCrudRepositoryImpl implements ICategoryRepository {

    private final ICategoryCrudRepository _repository;
    private final CategoryMapper _mapper;

    @Override
    public Iterable<Category> findAll() {
        return _mapper.toCategories(_repository.findAll());
    }

    @Override
    public Optional<Category> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }

        return _repository.findById(id).map(_mapper::toCategory);
    }

    @Override
    public Category save(Category category) {
        var entity = _mapper.toCategoryEntity(category);
        var savedEntity = _repository.save(entity);
        return _mapper.toCategory(savedEntity);
    }

    @Override
    public void deleteById(Integer id) {
        _repository.deleteById(id);
    }
}
