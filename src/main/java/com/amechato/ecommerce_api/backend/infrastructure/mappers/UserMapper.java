package com.amechato.ecommerce_api.backend.infrastructure.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.amechato.ecommerce_api.backend.domain.models.User;
import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mappings(
            {
                    @Mapping(source = "id", target = "id"),
                    @Mapping(source = "username", target = "username"),
                    @Mapping(source = "firstName", target = "firstName"),
                    @Mapping(source = "lastName", target = "lastName"),
                    @Mapping(source = "email", target = "email"),
                    @Mapping(source = "address", target = "address"),
                    @Mapping(source = "cellphone", target = "cellphone"),
                    @Mapping(source = "password", target = "password"),
                    @Mapping(source = "userType", target = "userType"),
                    @Mapping(source = "createDate", target = "createDate"),
                    @Mapping(source = "updateDate", target = "updateDate")
            }
    )

    User toUser(UserEntity userEntity);
    Iterable<User> toUsers(Iterable<UserEntity> userEntities);

    @InheritInverseConfiguration
    UserEntity toUserEntity(User user);
}
