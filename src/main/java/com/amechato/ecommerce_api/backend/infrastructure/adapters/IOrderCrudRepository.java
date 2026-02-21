package com.amechato.ecommerce_api.backend.infrastructure.adapters;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.OrderEntity;
import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.UserEntity;

import jakarta.transaction.Transactional;

public interface IOrderCrudRepository extends CrudRepository<OrderEntity, Integer> {

    @Transactional
    @Modifying
    @Query("UPDATE OrderEntity o SET o.orderState = :state WHERE o.id = :id")
    void updateStateById(Integer id, String state);

    Iterable<OrderEntity> findByUserEntity(UserEntity userEntity);
}
