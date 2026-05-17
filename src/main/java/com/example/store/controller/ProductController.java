package com.example.store.controller;

/*
Add a new endpoint /products to model products which appear in an order:
A single order contains 1 or more products.
A product has an ID and a description.
Add a POST endpoint to create a product
Add a GET endpoint to return all products, and a specific product by ID
In both cases, also return a list of the order IDs which contain those products
Change the orders endpoint to return a list of products contained in the order
 */

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.mapper.ProductMapper;
import com.example.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @GetMapping
    public List<ProductDTO> getAllProducts(){
        var all = productService.findAll();
        return productMapper.productsToProductDTOs(all);
    }

    @GetMapping("/{id}")
    public Optional<ProductDTO> getProductById(@PathVariable Long id){
        return productService.findById(id).map(productMapper::productToProductDTO);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO createProduct(@RequestBody Product product){
        Product newProduct = productService.save(product);

        return productMapper.productToProductDTO(newProduct);
    }
}
