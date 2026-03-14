package com.amechato.ecommerce_api.backend.domain.ports;

import com.amechato.ecommerce_api.backend.domain.models.Order;
import java.util.Optional;

public interface IOrderRepository {

    Iterable<Order> findAll();

    Order findById(Integer id);

    Iterable<Order> findByUserId(Integer userId);

    Order save(Order order);

    Optional<Order> findByPaypalOrderId(String paypalOrderId);

    void updateStateById(Integer id, String state);
}
