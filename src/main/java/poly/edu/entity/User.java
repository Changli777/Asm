package poly.edu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank(message = "Chưa nhập username")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Email
    @NotBlank(message = "Chưa nhập email")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Chưa nhập password")
    @Column(name = "password", length = 255)
    private String password;

    @NotBlank(message = "Chưa nhập họ tên")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "gender", nullable = false)
    private Boolean gender;

    @NotNull(message = "Chưa nhập ngày sinh")
    @Past(message = "Ngày sinh không hợp lệ")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @NotBlank(message = "Chưa nhập số điện thoại")
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private String role = "USER";

    @Column(name = "provider", length = 20)
    @Builder.Default
    private String provider = "LOCAL";

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<CartItem> cartItems = new java.util.ArrayList<>();

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
