package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Product;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductStatsDAO extends JpaRepository<Product, Long> {

    /**
     * Thống kê tổng số lượng và doanh thu theo từng Danh mục sản phẩm.
     * Trả về List<Object[]>, mỗi Object[] chứa: [Tên Category, Tổng số lượng đã bán, Tổng doanh thu]
     */
    @Query("SELECT c.categoryName, SUM(p.soldCount), SUM(p.soldCount * p.price) " +
            "FROM Product p JOIN p.category c " +
            "GROUP BY c.categoryName " +
            "ORDER BY SUM(p.soldCount) DESC")
    List<Object[]> getCategorySalesStats();
}