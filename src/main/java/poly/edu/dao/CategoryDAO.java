package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDAO extends JpaRepository<Category, Long> {

    List<Category> findAll();

    Optional<Category> findById(Long id);

    Optional<Category> findByCategoryName(String categoryName);

    Category save(Category category);

    void deleteById(Long id);

    boolean existsById(Long id);

    long count();

    // ✅ Lấy danh mục có chứa sản phẩm liên quan (dùng khi cần hiển thị các sp cùng loại)
    @Query("""
        SELECT DISTINCT c FROM Category c 
        JOIN FETCH c.products p 
        WHERE (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    List<Category> findCategoriesByProductKeyword(@Param("keyword") String keyword);
}
