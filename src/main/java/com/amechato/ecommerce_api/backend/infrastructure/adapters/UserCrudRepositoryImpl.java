package com.amechato.ecommerce_api.backend.infrastructure.adapters;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.amechato.ecommerce_api.backend.domain.models.User;
import com.amechato.ecommerce_api.backend.domain.ports.IUserRepository;
import com.amechato.ecommerce_api.backend.infrastructure.mappers.UserMapper;
import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.UserEntity;

@Repository
public class UserCrudRepositoryImpl implements IUserRepository {

    private final IUserCrudRepository userCrudRepository;
    private final UserMapper userMapper;

    public UserCrudRepositoryImpl(IUserCrudRepository userCrudRepository, UserMapper userMapper) {
        this.userCrudRepository = userCrudRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = this.userMapper.toUserEntity(user);
        UserEntity savedEntity = this.userCrudRepository.save(entity);

        return this.userMapper.toUser(savedEntity);
    }

    @Override
    public Optional<User> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }

        return this.userCrudRepository.findById(id)
                .map(this.userMapper::toUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String normalized = email == null ? null : email.trim();

        if (normalized == null || normalized.isEmpty()) {
            return Optional.empty();
        }

        return this.userCrudRepository.findByEmail(normalized)
                .map(this.userMapper::toUser);
    }

}
