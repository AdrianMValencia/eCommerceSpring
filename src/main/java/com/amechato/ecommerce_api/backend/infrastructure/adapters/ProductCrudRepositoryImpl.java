package com.amechato.ecommerce_api.backend.infrastructure.adapters;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.amechato.ecommerce_api.backend.domain.models.Product;
import com.amechato.ecommerce_api.backend.domain.ports.IProductRepository;
import com.amechato.ecommerce_api.backend.infrastructure.mappers.ProductMapper;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ProductCrudRepositoryImpl implements IProductRepository {
    private final IProductCrudRepository _repository;
    private final ProductMapper _mapper;

    @Override
    public Iterable<Product> findAll() {
        return _mapper.toProducts(_repository.findAll());
    }

    @Override
    public Optional<Product> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }

        return _repository.findById(id).map(_mapper::toProduct);
    }

    @Override
    public Product save(Product product) {
        var entity = _mapper.toProductEntity(product);
        var savedEntity = _repository.save(entity);
        return _mapper.toProduct(savedEntity);
    }

    @Override
    public void deleteById(Integer id) {
        _repository.deleteById(id);
    }
}
