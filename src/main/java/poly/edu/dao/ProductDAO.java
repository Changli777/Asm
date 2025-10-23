package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // ✅ THÊM HÀM TÌM KIẾM SẢN PHẨM
    @Query("""
        SELECT p FROM Product p
        WHERE (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (
                (:minPrice IS NULL OR :maxPrice IS NULL)
                OR (p.price BETWEEN :minPrice AND :maxPrice)
              )
        ORDER BY p.createdAt DESC
    """)
    List<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );
}
