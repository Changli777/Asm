package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Order;
import poly.edu.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDAO extends JpaRepository<Order, Long> {

    List<Order> findByStatusAndUserOrderByCreatedAtDesc(String status, User user);
    List<Order> findByStatusAndUserOrderByCreatedAtAsc(String status, User user);

    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByUserOrderByCreatedAtAsc(User user);

    Optional<Order> findByOrderIdAndUser(Long orderId, User user);

    Optional<Order> findByOrderNumber(String orderNumber);
}
