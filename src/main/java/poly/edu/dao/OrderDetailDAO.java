package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail;
import java.util.List;

@Repository
public interface OrderDetailDAO extends JpaRepository<OrderDetail, Long> {
    // Tìm tất cả OrderDetail theo Order
    List<OrderDetail> findByOrder(Order order);

    // Đếm số sản phẩm trong đơn hàng
    Long countByOrder(Order order);
}