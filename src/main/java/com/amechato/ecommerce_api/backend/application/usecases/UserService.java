package com.amechato.ecommerce_api.backend.application.usecases;

import com.amechato.ecommerce_api.backend.domain.models.User;
import com.amechato.ecommerce_api.backend.domain.models.UserType;
import com.amechato.ecommerce_api.backend.domain.ports.IUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserService {
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User payload is required");
        }

        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim());
        }

        if (user.getUserType() == null) {
            user.setUserType(UserType.USER);
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (!user.getPassword().startsWith("$2a$")
                && !user.getPassword().startsWith("$2b$")
                && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return this.userRepository.save(user);
    }

    public User findById(Integer id) {
        return this.userRepository.findById(id).orElse(null);
    }

    public User findByEmail(String email) {
        return this.userRepository.findByEmail(email).orElse(null);
    }
}
