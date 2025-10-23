package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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


    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public Optional<User> createAndSendOtp(String email) {
        Optional<User> optionalUser = userDAO.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // 1. Xóa t?t c? token c? c?a user này
            tokenDAO.deleteAllByUser(user);

            // 2. T?o mã OTP m?i (6 ch? s?)
            String otpCode = generateOtp();
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5); // H?n 5 phút

            PasswordResetToken token = new PasswordResetToken(user, otpCode, expiryDate);
            tokenDAO.save(token);

            // 3. G?i email OTP
            mailerService.sendOtpEmail(user.getEmail(), otpCode);

            return Optional.of(user);
        }
        return Optional.empty();
    }

    public boolean validateOtp(User user, String otpCode) {
        Optional<PasswordResetToken> tokenOpt = tokenDAO.findByUserAndTokenCodeAndExpiryDateAfterAndIsUsed(
                user, otpCode, LocalDateTime.now(), false
        );
        return tokenOpt.isPresent();
    }

    /**
     * ??t l?i m?t kh?u và dánh d?u token là dã dùng.
     */
    public void resetPassword(User user, String newPassword, String otpCode) {
        Optional<PasswordResetToken> tokenOpt = tokenDAO.findByUserAndTokenCodeAndExpiryDateAfterAndIsUsed(
                user, otpCode, LocalDateTime.now(), false
        );

        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ, hoặc đã hết hạn !");
        }

        user.setPassword(newPassword);
        userDAO.save(user);

        // Dánh d?u token là dã s? d?ng
        PasswordResetToken token = tokenOpt.get();
        token.setIsUsed(true);
        tokenDAO.save(token);
    }
}