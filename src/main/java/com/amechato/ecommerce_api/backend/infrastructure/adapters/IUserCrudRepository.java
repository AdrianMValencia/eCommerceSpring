package com.amechato.ecommerce_api.backend.infrastructure.adapters;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.UserEntity;

public interface IUserCrudRepository extends CrudRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String email);
}
