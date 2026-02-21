package com.amechato.ecommerce_api.backend.infrastructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.amechato.ecommerce_api.backend.domain.models.OrderDetail;
import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.OrderDetailEntity;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "quantity", target = "quantity"),
            @Mapping(source = "price", target = "price"),
            @Mapping(source = "productId", target = "productId")
    })

    OrderDetail toOrderDetail(OrderDetailEntity orderDetailEntity);

    Iterable<OrderDetail> toOrderDetails(Iterable<OrderDetailEntity> orderDetailEntities);
}
