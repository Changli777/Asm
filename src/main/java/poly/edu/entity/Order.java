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
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "status", length = 20)
    private String status = "Pending";

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    @Column(name = "payment_transaction_id", length = 255)
    private String paymentTransactionId;

    @Column(name = "shipping_full_name", nullable = false, length = 100)
    private String shippingFullName;

    @Column(name = "shipping_phone", nullable = false, length = 20)
    private String shippingPhone;

    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Column(name = "shipping_notes", length = 500)
    private String shippingNotes;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "order_date")
    private Date orderDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "confirmed_date")
    private Date confirmedDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "shipped_date")
    private Date shippedDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "delivered_date")
    private Date deliveredDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completed_date")
    private Date completedDate;

    @Column(name = "customer_confirmed")
    private Boolean customerConfirmed;

    @Column(name = "admin_confirmed")
    private Boolean adminConfirmed;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "cancelled_date")
    private Date cancelledDate;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details = new ArrayList<>();

    // Thêm trường transient cho chuỗi tổng tiền đã format
    @Transient
    private String formattedTotalAmount;

    @PrePersist
    public void prePersist() {
        if (orderNumber == null) {
            orderNumber = "ORD-" + System.currentTimeMillis();
        }
        if (orderDate == null) {
            orderDate = new Date();
        }
        if (createdAt == null) {
            createdAt = new Date();
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
    }

    @Transient
    public BigDecimal getTotal() {
        if (details == null || details.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return details.stream()
                .map(OrderDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}