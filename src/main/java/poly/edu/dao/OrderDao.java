package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Order;

@Repository
public interface OrderDao  extends JpaRepository<Order, Long> {}
