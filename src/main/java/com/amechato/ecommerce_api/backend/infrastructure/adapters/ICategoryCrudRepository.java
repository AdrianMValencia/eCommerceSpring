package com.amechato.ecommerce_api.backend.infrastructure.adapters;

import org.springframework.data.repository.CrudRepository;

import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.CategoryEntity;

public interface ICategoryCrudRepository extends CrudRepository<CategoryEntity, Integer> {

}
