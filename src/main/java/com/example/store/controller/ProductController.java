package com.example.store.controller;



import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.mapper.ProductMapper;
import com.example.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

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
    public Page<ProductDTO> getAllProducts(@PageableDefault(size = 20) Pageable pageable){
        log.debug("getAllProducts called");
        var page = productService.findAll(pageable);
        log.info("returning page {} of products with {} items", page.getNumber(), page.getNumberOfElements());
        return page.map(productMapper::productToProductDTO);
    }

    @GetMapping("/{id}")
    public Optional<ProductDTO> getProductById(@PathVariable Long id){
        log.debug("getProductById called id={}", id);
        return productService.findById(id).map(productMapper::productToProductDTO);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO createProduct(@RequestBody Product product){
        log.info("creating product description={}", product.getDescription());
        Product newProduct = productService.save(product);
        log.debug("created product id={}", newProduct.getId());
        return productMapper.productToProductDTO(newProduct);
    }
}
