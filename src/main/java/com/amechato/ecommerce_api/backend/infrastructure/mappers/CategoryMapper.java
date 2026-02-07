package com.amechato.ecommerce_api.backend.infrastructure.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.amechato.ecommerce_api.backend.domain.models.Category;
import com.amechato.ecommerce_api.backend.infrastructure.persistence.entities.CategoryEntity;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "name", target = "name"),
            @Mapping(source = "createDate", target = "createDate"),
            @Mapping(source = "updateDate", target = "updateDate")
    })

    Category toCategory(CategoryEntity categoryEntity);

    Iterable<Category> toCategories(Iterable<CategoryEntity> categoryEntities);

    @InheritInverseConfiguration
    CategoryEntity toCategoryEntity(Category category);
}
