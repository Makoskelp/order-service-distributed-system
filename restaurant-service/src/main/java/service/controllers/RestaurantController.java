package service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import service.core.Restaurant;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

interface RestaurantRepository extends JpaRepository<Restaurant, Long> {}

@CrossOrigin(origins = "*")
@RestController
public class RestaurantController {

    @Autowired
    private RestaurantRepository restaurantRepo;

    @GetMapping(value = "/restaurants", produces = "application/json")
    public ResponseEntity<List<String>> getAll() {
        List<String> links = restaurantRepo.findAll().stream()
            .map(r -> ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(r.getId())
                .toUriString())
            .collect(Collectors.toList());
        return ResponseEntity.ok(links);
    }

    @GetMapping(value = "/restaurants/{id}", produces = "application/json")
    public ResponseEntity<Restaurant> getById(@PathVariable Long id) {
        return restaurantRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping(value = "/restaurants", consumes = "application/json")
    public ResponseEntity<Restaurant> create(@RequestBody Restaurant restaurant) {
        Restaurant saved = restaurantRepo.save(restaurant);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(saved.getId())
            .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping(value = "/restaurants/{id}/menu", produces = "application/json")
    public ResponseEntity<List<String>> getMenu(@PathVariable Long id) {
        return restaurantRepo.findById(id)
            .map(r -> ResponseEntity.ok(r.getMenu()))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
