package com.amechato.ecommerce_api.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public UserService userService(IUserRepository userRepository) {
        return new UserService(userRepository);
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
