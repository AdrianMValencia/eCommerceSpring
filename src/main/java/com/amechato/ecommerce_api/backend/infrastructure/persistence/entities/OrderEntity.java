package com.amechato.ecommerce_api.backend.infrastructure.persistence.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.amechato.ecommerce_api.backend.domain.models.OrderState;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreationTimestamp
    private LocalDateTime orderDate;

    @OneToMany(mappedBy = "orderEntity", cascade = CascadeType.PERSIST)
    private List<OrderDetailEntity> orderDetails;

    @Enumerated(value = EnumType.STRING)
    private OrderState orderState;

    @Column(length = 30)
    private String paymentProvider;

    @Column(length = 30)
    private String paymentStatus;

    @Column(unique = true)
    private String paypalOrderId;

    private String paypalCaptureId;

    private LocalDateTime paidAt;

    @ManyToOne
    private UserEntity userEntity;
}
