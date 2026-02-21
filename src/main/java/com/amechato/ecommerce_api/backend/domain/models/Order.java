package com.amechato.ecommerce_api.backend.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Order {
    private Integer id;
    private LocalDateTime orderDate;
    private List<OrderDetail> orderDetails;
    private OrderState orderState;
    private Integer userId;


    public Order() {
        orderDetails = new ArrayList<>();
    }

    public BigDecimal getTotalOrderPrice() {
        return orderDetails.stream()
                .map(OrderDetail::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
