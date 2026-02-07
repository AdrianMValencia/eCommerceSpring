package com.amechato.ecommerce_api.backend.infrastructure.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.amechato.ecommerce_api.backend.domain.models.Product;
import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "name", target = "name"),
            @Mapping(source = "code", target = "code"),
            @Mapping(source = "description", target = "description"),
            @Mapping(source = "urlImage", target = "urlImage"),
            @Mapping(source = "price", target = "price"),
            @Mapping(source = "createDate", target = "createDate"),
            @Mapping(source = "updateDate", target = "updateDate"),
            @Mapping(source = "userEntity.id", target = "userId"),
            @Mapping(source = "categoryEntity.id", target = "categoryId")
    })

    Product toProduct(ProductEntity productEntity);

    Iterable<Product> toProducts(Iterable<ProductEntity> productEntities);

    @InheritInverseConfiguration
    ProductEntity toProductEntity(Product product);
}
