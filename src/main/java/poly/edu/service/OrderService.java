package poly.edu.service;

import poly.edu.entity.Order;
import poly.edu.entity.User;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> findByStatusAndUser(String status, User user, String sort);
    Optional<Order> findByOrderIdAndUser(Long orderId, User user);
    boolean cancelOrder(Long orderId, User user, String reason);
    Order save(Order order);
    Optional<Order> findByOrderNumber(String orderNumber);
}