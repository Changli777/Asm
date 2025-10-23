package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_code", nullable = false, length = 6)
    private String tokenCode;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "is_used")
    private Boolean isUsed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public PasswordResetToken(User user, String tokenCode, LocalDateTime expiryDate) {
        this.user = user;
        this.tokenCode = tokenCode;
        this.expiryDate = expiryDate;
    }
}