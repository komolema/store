package com.example.store.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "\"order\"")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    private Customer customer;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "orders")
    private Set<Product> products = new HashSet<>();
}
