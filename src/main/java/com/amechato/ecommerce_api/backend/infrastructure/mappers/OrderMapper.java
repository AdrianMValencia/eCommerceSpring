package com.amechato.ecommerce_api.backend.infrastructure.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.amechato.ecommerce_api.backend.domain.models.Order;
import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.OrderEntity;

@Mapper(componentModel = "spring", uses = { OrderDetailMapper.class })
public interface OrderMapper {

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "orderDate", target = "orderDate"),
            @Mapping(source = "orderDetails", target = "orderDetails"),
            @Mapping(source = "orderState", target = "orderState"),
            @Mapping(source = "userEntity.id", target = "userId")
    })

    Order toOrder(OrderEntity orderEntity);

    Iterable<Order> toOrders(Iterable<OrderEntity> orderEntities);

    @InheritInverseConfiguration
    OrderEntity toOrderEntity(Order order);
}
