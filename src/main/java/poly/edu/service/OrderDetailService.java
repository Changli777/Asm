package poly.edu.service;

import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail;

import java.util.List;

public interface OrderDetailService {

    // Tìm tất cả OrderDetail theo Order
    List<OrderDetail> findByOrder(Order order);

    // Đếm số sản phẩm trong đơn
    Long countByOrder(Order order);

    // Lưu OrderDetail
    OrderDetail save(OrderDetail orderDetail);
}