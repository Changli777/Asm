package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.OrderDao;
import poly.edu.dao.OrderDetailDao;
import poly.edu.entity.*;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderDetailDao orderDetailDao;

    @Autowired
    private CartItemService cartItemService;

    @Transactional
    public Order checkout(User user) {
        // Lấy danh sách giỏ hàng của user
        List<CartItem> cartItems = cartItemService.findAllByUser(user);
        if (cartItems == null || cartItems.isEmpty()) {
            return null;
        }

        // Tính tổng tiền đơn hàng
        BigDecimal total = cartItems.stream()
                .map(item -> {
                    BigDecimal price = item.getProduct().getDiscountPrice() != null
                            ? item.getProduct().getDiscountPrice()
                            : item.getProduct().getPrice();
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tạo mới đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setTotal(total);
        order.setStatus("Pending");
        order = orderDao.save(order); // lưu để có ID cho order

        // Lưu chi tiết đơn hàng
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());

            BigDecimal price = item.getProduct().getDiscountPrice() != null
                    ? item.getProduct().getDiscountPrice()
                    : item.getProduct().getPrice();

            detail.setPrice(price);
            orderDetailDao.save(detail);
        }

        // Xóa giỏ hàng sau khi checkout
        cartItemService.deleteAllByUser(user);

        return order;
    }
}
