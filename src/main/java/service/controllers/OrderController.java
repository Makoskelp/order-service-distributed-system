package service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.core.Order;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;

@RestController
public class OrderController {

    private Map<Long, Order> orders = new TreeMap<>();
    private Long nextId = 1L;

    @GetMapping(value = "/orders", produces = "application/json")
    public ResponseEntity<Collection<Order>> getOrders() {
        return ResponseEntity.status(HttpStatus.OK).body(orders.values());
    }

    @GetMapping(value = "/orders/{id}", produces = "application/json")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orders.get(id);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    @PostMapping(value = "/orders", consumes = "application/json")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        order.setId(nextId);
        order.setStatus("PLACED");
        orders.put(nextId, order);
        nextId++;
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PatchMapping(value = "/orders/{id}", consumes = "application/json")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Order order = orders.get(id);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        order.setStatus(body.get("status"));
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }
}