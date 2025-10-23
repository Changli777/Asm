package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.OrderDAO;
import poly.edu.entity.Order;
import poly.edu.entity.User;
import poly.edu.service.OrderService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDAO orderDAO;

    @Override
    public List<Order> findByStatusAndUser(String status, User user, String sort) {
        if ("oldest".equalsIgnoreCase(sort)) {
            return orderDAO.findByStatusAndUserOrderByCreatedAtAsc(status, user);
        }
        return orderDAO.findByStatusAndUserOrderByCreatedAtDesc(status, user);
    }

    @Override
    public Optional<Order> findByOrderIdAndUser(Long orderId, User user) {
        return orderDAO.findByOrderIdAndUser(orderId, user);
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, User user, String reason) {
        Optional<Order> optionalOrder = orderDAO.findByOrderIdAndUser(orderId, user);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            if ("Pending".equalsIgnoreCase(order.getStatus())) {
                order.setStatus("Cancelled");
                order.setCancelledDate(new Date());
                order.setCancellationReason(reason);
                orderDAO.save(order);
                return true;
            }
        }
        return false;
    }

    @Override
    public Order save(Order order) {
        return orderDAO.save(order);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderDAO.findByOrderNumber(orderNumber);
    }
}