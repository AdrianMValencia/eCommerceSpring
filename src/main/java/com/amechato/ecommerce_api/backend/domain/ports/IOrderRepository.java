package com.amechato.ecommerce_api.backend.domain.ports;

import com.amechato.ecommerce_api.backend.domain.models.Order;

public interface IOrderRepository {

    Iterable<Order> findAll();

    Order findById(Integer id);

    Iterable<Order> findByUserId(Integer userId);

    Order save(Order order);

    void updateStateById(Integer id, String state);
}
