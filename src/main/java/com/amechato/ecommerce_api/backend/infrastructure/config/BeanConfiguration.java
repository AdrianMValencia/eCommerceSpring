package com.amechato.ecommerce_api.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.amechato.ecommerce_api.backend.application.usecases.CategoryService;
import com.amechato.ecommerce_api.backend.application.usecases.OrderService;
import com.amechato.ecommerce_api.backend.application.usecases.ProductService;
import com.amechato.ecommerce_api.backend.application.usecases.UserService;
import com.amechato.ecommerce_api.backend.domain.ports.ICategoryRepository;
import com.amechato.ecommerce_api.backend.domain.ports.IOrderRepository;
import com.amechato.ecommerce_api.backend.domain.ports.IProductRepository;
import com.amechato.ecommerce_api.backend.domain.ports.IUserRepository;

@Configuration
public class BeanConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserService userService(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new UserService(userRepository, passwordEncoder);
    }

    @Bean
    public CategoryService categoryService(ICategoryRepository categoryRepository) {
        return new CategoryService(categoryRepository);
    }

    @Bean
    public ProductService productService(IProductRepository productRepository) {
        return new ProductService(productRepository);
    }

    @Bean
    public OrderService orderService(IOrderRepository orderRepository) {
        return new OrderService(orderRepository);
    }
}
