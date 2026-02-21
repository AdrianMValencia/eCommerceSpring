package com.amechato.ecommerce_api.backend.infrastructure.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amechato.ecommerce_api.backend.application.usecases.OrderService;
import com.amechato.ecommerce_api.backend.domain.models.Order;
import com.amechato.ecommerce_api.backend.domain.models.OrderState;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService _orderService;

    public OrderController(OrderService orderService) {
        _orderService = orderService;
    };

    @GetMapping
    public ResponseEntity<Iterable<Order>> findAll() {
        return ResponseEntity.ok(_orderService.findAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<Order> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(_orderService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Iterable<Order>> findByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(_orderService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Order> save(@RequestBody Order order) {
        if (order.getOrderState().toString().equals(OrderState.CANCELLED.toString())) {
            order.setOrderState(OrderState.CANCELLED);
        } else {
            order.setOrderState(OrderState.CONFIRMED);
        }

        return ResponseEntity.ok(_orderService.save(order));
    }

    @PostMapping("/{id}/state")
    public ResponseEntity<Void> updateStateById(@PathVariable Integer id, @RequestParam String state) {
        _orderService.updateStateById(id, state);
        return ResponseEntity.ok().build();
    }
}
