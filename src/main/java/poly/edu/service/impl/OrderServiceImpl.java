package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import poly.edu.dao.OrderDAO;
import poly.edu.entity.Order;
import poly.edu.entity.User;
import poly.edu.service.OrderService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    @Override
    public List<Order> findByStatus(String status) {
        return orderDAO.findByStatus(status);
    }

    @Override
    public Order findById(Long id) {
        return orderDAO.findById(id).orElse(null);
    }

    @Override
    public long countAll() {
        return orderDAO.count();
    }

    @Override
    public void confirmOrder(Order order) {
        if (!"Pending".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái Pending");
        }
        order.setStatus("Confirmed");
        order.setConfirmedDate(new Date());
        orderDAO.save(order);
    }

    @Override
    public void startShipping(Order order) {
        if (!"Confirmed".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái Confirmed");
        }
        order.setStatus("Shipping");
        order.setShippedDate(new Date());
        orderDAO.save(order);
    }

    @Override
    public void markDelivered(Order order) {
        if (!"Shipping".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái Shipping");
        }
        order.setStatus("Delivered");
        order.setDeliveredDate(new Date());
        orderDAO.save(order);
    }

    @Override
    public void adminConfirmPayment(Order order) {
        if (!"Delivered".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái Delivered");
        }
        order.setAdminConfirmed(true);
        if(order.isAdminConfirmed() &&  order.isCustomerConfirmed()) {
            order.setStatus("Completed");
        }
        order.setCompletedDate(new Date());
        orderDAO.save(order);
    }

    @Override
    public void customerConfirmPayment(Order order) {
        if (!"Delivered".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái Delivered");
        }
        order.setCustomerConfirmed(true);
        if(order.isAdminConfirmed() &&  order.isCustomerConfirmed()) {
            order.setStatus("Completed");
        }
        order.setCompletedDate(new Date());
        orderDAO.save(order);
    }


    @Override
    public List<Order> findAll() {
        return orderDAO.findAll();
    }

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

}