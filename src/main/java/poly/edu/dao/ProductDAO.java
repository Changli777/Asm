package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import poly.edu.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductDAO extends JpaRepository<Product, Long> {

    List<Product> findAll();

    Optional<Product> findById(Long id);

    Optional<Product> findByProductName(String name);

    Product save(Product product);

    void deleteById(Long id);

    boolean existsById(Long id);

    long count();

    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findNewProducts();

    @Query("SELECT p FROM Product p WHERE p.discountPrice > 0 ORDER BY p.discountPrice DESC")
    List<Product> findSaleProducts();

    @Query("SELECT p FROM Product p WHERE p.isFeatured = true")
    List<Product> findFeaturedProducts();

}