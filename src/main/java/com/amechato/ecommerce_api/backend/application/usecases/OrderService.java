package com.amechato.ecommerce_api.backend.application.usecases;

import com.amechato.ecommerce_api.backend.domain.models.Order;
import com.amechato.ecommerce_api.backend.domain.ports.IOrderRepository;
import java.util.Optional;

public class OrderService {

    private final IOrderRepository _repository;

    public OrderService(IOrderRepository repository) {
        _repository = repository;
    }

    public Iterable<Order> findAll() {
        return _repository.findAll();
    }

    public Order findById(Integer id) {
        return _repository.findById(id);
    }

    public Iterable<Order> findByUserId(Integer userId) {
        return _repository.findByUserId(userId);
    }

    public Order save(Order order) {
        return _repository.save(order);
    }

    public Optional<Order> findByPaypalOrderId(String paypalOrderId) {
        return _repository.findByPaypalOrderId(paypalOrderId);
    }

    public void updateStateById(Integer id, String state) {
        _repository.updateStateById(id, state);
    }

}
