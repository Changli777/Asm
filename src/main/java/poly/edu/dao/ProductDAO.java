package poly.edu.dao;

import poly.edu.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductDAO {

    List<Product> findAll();
    Optional<Product> findById(Long id);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByNameContaining(String keyword);
    List<Product> findNewProducts(int limit);
    List<Product> findSaleProducts(int limit);
    List<Product> findFeaturedProducts(int limit);
    List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> searchProducts(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);
    Product save(Product product);
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
    void incrementViewCount(Long productId);
    void incrementSoldCount(Long productId, int quantity);
    List<Product> findNewProducts();
    List<Product> findSaleProducts();
    List<Product> findFeaturedProducts();
}