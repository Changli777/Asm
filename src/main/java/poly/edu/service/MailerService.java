package poly.edu.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailerService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Gửi email OTP đến người dùng.
     * @param toEmail Email người nhận
     * @param otpCode Mã OTP
     */
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

            helper.setTo(toEmail);
            helper.setSubject("Mã OTP Đặt lại Mật khẩu của bạn");

            String htmlContent = String.format("""
                <html>
                <body>
                    <p>Mã OTP (One-Time Password) của bạn để đặt lại mật khẩu là:</p>
                    <h2 style="color:#667eea; font-size:24px; text-align:center;">%s</h2>
                    <p>Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>
                    <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
                </body>
                </html>
                """, otpCode);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ Email sent to: " + toEmail + " with OTP: " + otpCode);
        } catch (MessagingException e) {
            System.err.println("❌ Lỗi khi gửi email OTP: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email OTP. Vui lòng thử lại sau.", e);
        }
    }
}