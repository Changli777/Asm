package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Order;
import poly.edu.entity.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDAO extends JpaRepository<Order, Long> {

    // Tìm đơn hàng theo status và user, sắp xếp theo ngày tạo
    List<Order> findByStatusAndUserOrderByCreatedAtDesc(String status, User user);
    List<Order> findByStatusAndUserOrderByCreatedAtAsc(String status, User user);

    // Tìm tất cả đơn hàng của user, sắp xếp theo ngày
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByUserOrderByCreatedAtAsc(User user);

    // Tìm theo orderId và user (để bảo mật, user chỉ xem được đơn của mình)
    Optional<Order> findByOrderIdAndUser(Long orderId, User user);

    // Tìm theo orderNumber
    Optional<Order> findByOrderNumber(String orderNumber);
}
