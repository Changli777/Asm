package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.PasswordResetTokenDAO;
import poly.edu.dao.UserDAO;
import poly.edu.entity.PasswordResetToken;
import poly.edu.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class PasswordResetService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PasswordResetTokenDAO tokenDAO;

    @Autowired
    private MailerService mailerService;

    @Autowired
    private PasswordEncoder passwordEncoder; // 🔥 Thêm password encoder

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public Optional<User> createAndSendOtp(String email) {
        Optional<User> optionalUser = userDAO.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Xóa toàn bộ token cũ
            tokenDAO.deleteAllByUser(user);

            // Tạo mã OTP
            String otpCode = generateOtp();
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);

            PasswordResetToken token = new PasswordResetToken(user, otpCode, expiryDate);
            tokenDAO.save(token);

            // Gửi email OTP
            mailerService.sendOtpEmail(user.getEmail(), otpCode);

            return Optional.of(user);
        }
        return Optional.empty();
    }

    public boolean validateOtp(User user, String otpCode) {
        Optional<PasswordResetToken> tokenOpt = tokenDAO
                .findByUserAndTokenCodeAndExpiryDateAfterAndIsUsed(
                        user,
                        otpCode,
                        LocalDateTime.now(),
                        false
                );
        return tokenOpt.isPresent();
    }

    public void resetPassword(User user, String newPassword, String otpCode) {

        Optional<PasswordResetToken> tokenOpt = tokenDAO
                .findByUserAndTokenCodeAndExpiryDateAfterAndIsUsed(
                        user,
                        otpCode,
                        LocalDateTime.now(),
                        false
                );

        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn!");
        }

        // 🔥 MÃ HÓA PASSWORD MỚI (QUAN TRỌNG!)
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userDAO.save(user);

        // Đánh dấu token là đã dùng
        PasswordResetToken token = tokenOpt.get();
        token.setIsUsed(true);
        tokenDAO.save(token);
    }
}
