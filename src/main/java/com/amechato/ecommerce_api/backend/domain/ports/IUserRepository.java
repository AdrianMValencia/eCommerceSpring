package com.amechato.ecommerce_api.backend.domain.ports;

import java.util.Optional;

import com.amechato.ecommerce_api.backend.domain.models.User;

public interface IUserRepository {
    User save(User user);
    Optional<User> findById(Integer id);
    Optional<User> findByEmail(String email);
}
