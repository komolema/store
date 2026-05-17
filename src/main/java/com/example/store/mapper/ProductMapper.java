package com.example.store.mapper;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDTO productToProductDTO(Product product);

    List<ProductDTO> productsToProductDTOs(List<Product> products);

    @AfterMapping
    default void populateOrderIds(Product product, @MappingTarget ProductDTO productDTO) {
        if (product.getOrders() != null) {
            productDTO.setOrderIds(
                product.getOrders().stream()
                    .map(Order::getId)
                    .collect(Collectors.toList())
            );
        }
    }
}
