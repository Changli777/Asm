package poly.edu.dao;

import poly.edu.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductDAO {

    List<Product> findAll();

    Optional<Product> findById(Long id);

    List<Product> findByCategoryId(Long categoryId);

    // Tìm products theo tên (LIKE search)
    List<Product> findByNameContaining(String keyword);

    // Lấy sản phẩm mới (is_new = true)
    List<Product> findNewProducts(int limit);

    // Lấy sản phẩm giảm giá (is_on_sale = true)
    List<Product> findSaleProducts(int limit);

    // Lấy sản phẩm nổi bật/bán chạy (is_featured = true)
    List<Product> findFeaturedProducts(int limit);

    // Tìm theo khoảng giá
    List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    // Tìm kiếm với nhiều điều kiện
    List<Product> searchProducts(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);

    // Lưu product (insert hoặc update)
    Product save(Product product);

    // Xóa product theo ID
    void deleteById(Long id);

    // Kiểm tra product có tồn tại không
    boolean existsById(Long id);

    // Đếm số lượng products
    long count();

    // Tăng views count
    void incrementViewCount(Long productId);

    // Tăng sold count
    void incrementSoldCount(Long productId, int quantity);
}