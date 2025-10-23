package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.PasswordResetToken;
import poly.edu.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenDAO extends JpaRepository<PasswordResetToken, Long> {

    // Tìm token hợp lệ (chưa hết hạn và chưa được sử dụng)
    Optional<PasswordResetToken> findByUserAndTokenCodeAndExpiryDateAfterAndIsUsed(
            User user, String tokenCode, LocalDateTime now, Boolean isUsed
    );

    // Xóa tất cả token cũ của user
    void deleteAllByUser(User user);
}