package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.OrderDetailDAO;
import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail;
import poly.edu.service.OrderDetailService;

import java.util.List;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    @Autowired
    private OrderDetailDAO orderDetailDAO;

    @Override
    public List<OrderDetail> findByOrder(Order order) {
        return orderDetailDAO.findByOrder(order);
    }

    @Override
    public Long countByOrder(Order order) {
        return orderDetailDAO.countByOrder(order);
    }

    @Override
    public OrderDetail save(OrderDetail orderDetail) {
        return orderDetailDAO.save(orderDetail);
    }
}