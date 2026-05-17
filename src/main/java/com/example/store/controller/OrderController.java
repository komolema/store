package com.example.store.controller;

import com.example.store.dto.OrderDTO;
import com.example.store.entity.Order;
import com.example.store.mapper.OrderMapper;
import com.example.store.service.OrderService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @GetMapping
    public List<OrderDTO> getAllOrders() {
        log.debug("getAllOrders called");
        var all = orderService.findAll();
        log.info("returning {} orders", all.size());
        return orderMapper.ordersToOrderDTOs(all);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDTO createOrder(@RequestBody Order order) {
        Long customerId = null;
        if (order.getCustomer() != null) {
            customerId = order.getCustomer().getId();
        }
        log.info("creating order description={} customerId={}", order.getDescription(), customerId);
        var saved = orderService.save(order);
        log.debug("created order id={}", saved.getId());
        return orderMapper.orderToOrderDTO(saved);
    }

    @GetMapping("/{id}")
    public Optional<OrderDTO> getOrderById(@PathVariable Long id){
        return orderService.findById(id).map(orderMapper::orderToOrderDTO);
    }
}
