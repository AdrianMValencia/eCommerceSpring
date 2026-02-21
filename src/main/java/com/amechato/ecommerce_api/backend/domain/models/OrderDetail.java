package com.amechato.ecommerce_api.backend.domain.models;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderDetail {
    private Integer id;
    private BigDecimal quantity;
    private BigDecimal price;
    private Integer productId;

    public BigDecimal getTotalPrice() {
        return this.price.multiply(this.quantity);
    }
}
