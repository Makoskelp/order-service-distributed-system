package service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "restaurant-service", fallbackFactory = RestaurantClientFallbackFactory.class)
public interface RestaurantClient {

    @GetMapping("/restaurants/{id}")
    ResponseEntity<Object> getRestaurantById(@PathVariable Long id);

    @GetMapping("/restaurants/{id}/menu")
    ResponseEntity<List<String>> getMenu(@PathVariable Long id);
}
