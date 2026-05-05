package service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import service.core.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
