package com.amechato.ecommerce_api.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amechato.ecommerce_api.backend.application.usecases.UserService;
import com.amechato.ecommerce_api.backend.domain.ports.IUserRepository;

@Configuration
public class BeanConfiguration {

    @Bean
    public UserService userService(IUserRepository userRepository) {
        return new UserService(userRepository);
    }
}
