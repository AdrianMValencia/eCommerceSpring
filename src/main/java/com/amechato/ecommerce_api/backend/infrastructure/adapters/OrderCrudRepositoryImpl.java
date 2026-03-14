package com.amechato.ecommerce_api.backend.infrastructure.adapters;

import org.springframework.stereotype.Repository;

import com.amechato.ecommerce_api.backend.domain.models.Order;
import com.amechato.ecommerce_api.backend.domain.models.OrderState;
import com.amechato.ecommerce_api.backend.domain.ports.IOrderRepository;
import com.amechato.ecommerce_api.backend.infrastructure.mappers.OrderMapper;
import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.OrderEntity;
import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.UserEntity;
import java.util.Optional;

@Repository
public class OrderCrudRepositoryImpl implements IOrderRepository {

    private final OrderMapper _mapper;
    private final IOrderCrudRepository _repository;

    public OrderCrudRepositoryImpl(OrderMapper mapper, IOrderCrudRepository repository) {
        _mapper = mapper;
        _repository = repository;
    }

    @Override
    public Iterable<Order> findAll() {
        return _mapper.toOrders(_repository.findAll());
    }

    @Override
    public Order findById(Integer id) {
        return _mapper.toOrder(_repository.findById(id).orElseThrow(
                () -> new RuntimeException("Order not found with id: " + id)));
    }

    @Override
    public Iterable<Order> findByUserId(Integer userId) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        return _mapper.toOrders(_repository.findByUserEntity(userEntity));
    }

    @Override
    public Order save(Order order) {
        OrderEntity orderEntity = _mapper.toOrderEntity(order);
        if (orderEntity.getOrderDetails() != null) {
            orderEntity.getOrderDetails().forEach(detail -> detail.setOrderEntity(orderEntity));
        }
        return _mapper.toOrder(_repository.save(orderEntity));
    }

    @Override
    public Optional<Order> findByPaypalOrderId(String paypalOrderId) {
        return _repository.findByPaypalOrderId(paypalOrderId).map(_mapper::toOrder);
    }

    @Override
    public void updateStateById(Integer id, String state) {
        try {
            _repository.updateStateById(id, OrderState.valueOf(state).name());
        } catch (IllegalArgumentException ex) {
            _repository.updateStateById(id, OrderState.CONFIRMED.name());
        }
    }
}
