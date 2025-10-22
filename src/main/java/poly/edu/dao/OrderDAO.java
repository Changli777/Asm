package poly.edu.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Order;
import poly.edu.entity.User;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDAO extends JpaRepository<Order, Long> {

    List<Order> findByStatus(String status);

    // Tìm đơn hàng theo status và user, sắp xếp theo ngày tạo
    List<Order> findByStatusAndUserOrderByCreatedAtDesc(String status, User user);
    List<Order> findByStatusAndUserOrderByCreatedAtAsc(String status, User user);

    // Tìm theo orderId và user (để bảo mật, user chỉ xem được đơn của mình)
    Optional<Order> findByOrderIdAndUser(Long orderId, User user);

    // Tìm theo orderNumber
    Optional<Order> findByOrderNumber(String orderNumber);

    // lấy tất cả (no username filter) sắp xếp
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findAllByOrderByCreatedAtAsc();

    // tìm theo username (partial, case-insensitive), sắp xếp
    List<Order> findByUser_UsernameContainingIgnoreCaseOrderByCreatedAtDesc(String username);
    List<Order> findByUser_UsernameContainingIgnoreCaseOrderByCreatedAtAsc(String username);

}
