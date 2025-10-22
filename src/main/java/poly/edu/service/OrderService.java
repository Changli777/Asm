package poly.edu.service;

import org.springframework.stereotype.Service;
import poly.edu.entity.*;
import java.util.Optional;
import java.util.List;

@Service
public interface OrderService {

    List<Order> findAll();

    // Tìm đơn hàng theo status và user
    List<Order> findByStatusAndUser(String status, User user, String sort);
    // Tìm đơn hàng theo ID và user
    Optional<Order> findByOrderIdAndUser(Long orderId, User user);
    // Hủy đơn hàng
    boolean cancelOrder(Long orderId, User user, String reason);
    // Lưu đơn hàng
    Order save(Order order);
    List<Order> findByStatus(String status);
    Order findById(Long id);
    long countAll();
    void confirmOrder(Order order);              // Pending -> Confirmed
    void startShipping(Order order);             // Confirmed -> Shipping
    void markDelivered(Order order);             // Shipping -> Delivered
    void adminConfirmPayment(Order order);       // Delivered -> Admin xác nhận
    void customerConfirmPayment(Order order);
}
