package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poly.edu.entity.CartItem;
import poly.edu.entity.Product;
import poly.edu.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemDAO extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    @Query("SELECT COUNT(DISTINCT c.product) FROM CartItem c WHERE c.user = :user")
    long countDistinctByUser(User user);

    long countByUser(User user);

    void deleteAllByUser(User user);
}