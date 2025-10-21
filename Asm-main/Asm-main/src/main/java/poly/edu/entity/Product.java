package poly.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 18, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "is_new")
    private Boolean isNew;

    @Column(name = "is_on_sale")
    private Boolean isOnSale;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "views_count")
    private Integer viewsCount;

    @Column(name = "sold_count")
    private Integer soldCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (viewsCount == null) viewsCount = 0;
        if (soldCount == null) soldCount = 0;
        if (isFeatured == null) isFeatured = false;
        if (isNew == null) isNew = false;
        if (isOnSale == null) isOnSale = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Transient field - không lưu vào DB
    @Transient
    private String categoryName; // Dùng để hiển thị tên category

    @Transient
    private BigDecimal finalPrice; // Giá sau khi giảm

    // Method tính giá cuối cùng
    public BigDecimal getFinalPrice() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0) {
            return discountPrice;
        }
        return price;
    }

    // Method tính % giảm giá
    public Integer getDiscountPercent() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0 && price != null) {
            BigDecimal discount = price.subtract(discountPrice);
            BigDecimal percent = discount.divide(price, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            return percent.intValue();
        }
        return 0;
    }
}