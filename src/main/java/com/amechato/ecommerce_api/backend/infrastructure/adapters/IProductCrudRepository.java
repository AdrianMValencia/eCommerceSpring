package com.amechato.ecommerce_api.backend.infrastructure.adapters;

import org.springframework.data.repository.CrudRepository;

import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.ProductEntity;

public interface IProductCrudRepository extends CrudRepository<ProductEntity, Integer> {

}
