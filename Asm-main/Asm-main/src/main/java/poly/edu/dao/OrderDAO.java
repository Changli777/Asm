package poly.edu.dao;

import poly.edu.entity.Order;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    List<Order> findAll();
    Optional<Order> findById(Long id);
    Order save(Order order);
    void deleteById(Long id);
    long count();
    Double getTotalRevenue();
}
