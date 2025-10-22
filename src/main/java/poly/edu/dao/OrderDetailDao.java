package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.OrderDetail;

@Repository
public interface OrderDetailDao extends JpaRepository<OrderDetail, Long> {
}