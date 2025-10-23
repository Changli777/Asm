package poly.edu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Chưa nhập tên sản phẩm")
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "description", length = 2000)
    private String description;

    @NotNull(message = "Chưa nhập giá sản phẩm")
    @Min(value = 1000, message = "Giá sản phẩm tối thiểu là 1000")
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 18, scale = 2)
    private BigDecimal discountPrice;

    @NotNull(message = "Chưa nhập số lượng sản phẩm")
    @Min(value = 1, message = "Số lượng sản phẩm tối thiểu là 1")
    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    // ✅ Đảm bảo mapping tới Category có khóa chính là categoryId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", referencedColumnName = "category_id", nullable = false)
    private Category category;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "is_new")
    private Boolean isNew;

    @Column(name = "is_on_sale")
    private Boolean isOnSale;

    @Column(name = "image_url", length = 500)
    private String imageUrl; // Trường gốc dùng để lưu URL/tên file ảnh

    @Column(name = "views_count")
    private Integer viewsCount;

    @Column(name = "sold_count")
    private Integer soldCount;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<CartItem> cartItems = new java.util.ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Bổ sung Getter ảo để tương thích với Thymeleaf (nếu dùng ${p.image}) ---

    /**
     * Phương thức Getter ảo (Transient Getter) để hỗ trợ cú pháp cũ
     * ${p.image} trong Thymeleaf, nếu muốn tránh sửa search.html.
     * Nếu đã sửa search.html thành ${p.imageUrl} thì không cần thiết.
     */
    @Transient // Không ánh xạ vào CSDL
    public String getImage() {
        return this.imageUrl;
    }

    // --- Kết thúc phần bổ sung ---

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

    @Transient
    private String categoryName; // Dùng để hiển thị tên category

    @Transient
    private BigDecimal finalPrice; // Giá sau khi giảm

    // ✅ Giá cuối cùng (có giảm giá thì lấy discountPrice)
    public BigDecimal getFinalPrice() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0) {
            return discountPrice;
        }
        return price;
    }

    // ✅ % giảm giá
    public Integer getDiscountPercent() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0 && price != null) {
            BigDecimal discount = price.subtract(discountPrice);
            BigDecimal percent = discount.divide(price, 2, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            return percent.intValue();
        }
        return 0;
    }

    // ✅ Lấy tên category an toàn (tránh null pointer khi lazy)
    public String getCategoryName() {
        if (category != null) return category.getCategoryName();
        return categoryName;
    }

    // ✅ Lấy ID của category (để query không lỗi)
    public Long getCategoryId() {
        if (category != null) return category.getCategoryId();
        return null;
    }
}