package service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import service.core.User;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

interface UserRepository extends JpaRepository<User, Long> {}

@CrossOrigin(origins = "*")
@RestController
public class UserController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping(value = "/users", produces = "application/json")
    public ResponseEntity<List<String>> getAll() {
        List<String> links = userRepo.findAll().stream()
            .map(u -> ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(u.getId())
                .toUriString())
            .collect(Collectors.toList());
        return ResponseEntity.ok(links);
    }

    @GetMapping(value = "/users/{id}", produces = "application/json")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return userRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping(value = "/users", consumes = "application/json")
    public ResponseEntity<User> create(@RequestBody User user) {
        User saved = userRepo.save(user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(saved.getId())
            .toUri();
        return ResponseEntity.created(location).body(saved);
    }
}
