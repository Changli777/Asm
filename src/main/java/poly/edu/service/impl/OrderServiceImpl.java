package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.OrderDAO;
import poly.edu.dao.OrderDetailDAO; // <-- THÊM
import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail; // <-- THÊM
import poly.edu.entity.Product; // <-- THÊM
import poly.edu.entity.User;
import poly.edu.service.OrderService;
import poly.edu.service.ProductService; // <-- THÊM
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private OrderDetailDAO orderDetailDAO; // <-- CẦN THIẾT CHO LOGIC HOÀN LẠI

    @Autowired
    private ProductService productService; // <-- CẦN THIẾT CHO LOGIC HOÀN LẠI

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

    /**
     * Hủy đơn hàng, cập nhật trạng thái, và hoàn lại số lượng sản phẩm vào tồn kho và soldCount.
     */
    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, User user, String reason) {
        Optional<Order> optionalOrder = orderDAO.findByOrderIdAndUser(orderId, user);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            // Chỉ cho phép hủy nếu status = Pending
            if ("Pending".equalsIgnoreCase(order.getStatus())) {

                // 1. Cập nhật trạng thái đơn hàng
                order.setStatus("Cancelled");
                order.setCancelledDate(new Date());
                order.setCancellationReason(reason);
                orderDAO.save(order);

                // 2. Hoàn lại số lượng vào tồn kho và soldCount
                // (Sử dụng OrderDetailDAO để lấy chi tiết sản phẩm)
                List<OrderDetail> details = orderDetailDAO.findByOrder(order);
                for (OrderDetail detail : details) {
                    // Cần lấy lại Product qua Service để đảm bảo cập nhật Transactional
                    Product product = detail.getProduct();
                    int cancelledQuantity = detail.getQuantity();

                    // ✅ Hoàn lại vào soldCount (GIẢM số liệu thống kê)
                    if (product.getSoldCount() != null) {
                        product.setSoldCount(product.getSoldCount() - cancelledQuantity);
                    }

                    // ✅ Hoàn lại vào StockQuantity (TĂNG tồn kho)
                    product.setStockQuantity(product.getStockQuantity() + cancelledQuantity);

                    // Lưu Product đã cập nhật
                    productService.update(product);
                }

                return true;
            }
        }
        // Trả về false nếu không tìm thấy đơn hàng hoặc trạng thái không phải Pending
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