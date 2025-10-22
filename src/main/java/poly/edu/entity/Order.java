package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id") // 👈 đổi để đúng tên trong DB
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "order_date")
    private Date orderDate = new Date();

    // 👇 đổi lại cho trùng với cột có sẵn trong DB
    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal total;

    @Column(name = "status", length = 50)
    private String status = "Pending";

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details = new ArrayList<>();

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @PrePersist
    public void prePersist() {
        if (orderNumber == null) {
            orderNumber = "ORD" + System.currentTimeMillis();
        }
    }
}
