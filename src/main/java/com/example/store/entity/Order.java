package com.example.store.entity;

import jakarta.persistence.*;

import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "\"order\"")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "orders")
    private Set<Product> products = new HashSet<>();
}
