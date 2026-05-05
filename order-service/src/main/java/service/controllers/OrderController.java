package service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import service.core.Notification;
import org.springframework.web.bind.annotation.*;
import service.clients.RestaurantClient;
import service.clients.UserClient;
import service.core.Order;
import service.repositories.OrderRepository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final RestaurantClient restaurantClient;
    private final JmsTemplate jmsTemplate;

    public OrderController(OrderRepository orderRepository,
                           UserClient userClient,
                           RestaurantClient restaurantClient,
                           JmsTemplate jmsTemplate) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.restaurantClient = restaurantClient;
        this.jmsTemplate = jmsTemplate;
    }

    @GetMapping(value = "/orders", produces = "application/json")
    public ResponseEntity<List<String>> getOrders() {
        List<String> links = orderRepository.findAll().stream()
            .map(o -> ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(o.getId())
                .toUriString())
            .collect(Collectors.toList());
        return ResponseEntity.ok(links);
    }

    @GetMapping(value = "/orders/{id}", produces = "application/json")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(order.get());
    }

    @PostMapping(value = "/orders", consumes = "application/json")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        String host = "unknown";
        try { host = InetAddress.getLocalHost().getHostName(); } catch (Exception ignored) {}
        System.out.println("[" + host + "] Received order for user=" + order.getUserId());

        ResponseEntity<Object> userResponse = userClient.getUserById(order.getUserId());
        if (!userResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(userResponse.getStatusCode()).build();
        }

        ResponseEntity<Object> restaurantResponse = restaurantClient.getRestaurantById(order.getRestaurantId());
        if (!restaurantResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(restaurantResponse.getStatusCode()).build();
        }

        ResponseEntity<List<String>> menuResponse = restaurantClient.getMenu(order.getRestaurantId());
        if (!menuResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(menuResponse.getStatusCode()).build();
        }
        List<String> menu = menuResponse.getBody();
        for (String item : order.getItems()) {
            if (!menu.contains(item)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }

        order.setStatus("PLACED");
        Order saved = orderRepository.save(order);

        Notification notification = new Notification();
        notification.setOrderId(saved.getId());
        notification.setUserId(saved.getUserId());
        notification.setStatus("PLACED");
        notification.setMessage("Order placed successfully");
        jmsTemplate.send("ORDERS", session -> session.createObjectMessage(notification));

        java.net.URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(saved.getId())
            .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PatchMapping(value = "/orders/{id}", consumes = "application/json")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Optional<Order> optional = orderRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Order order = optional.get();
        order.setStatus(body.get("status"));
        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }
}
