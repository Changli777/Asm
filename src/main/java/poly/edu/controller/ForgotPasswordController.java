package poly.edu.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.User;
import poly.edu.service.PasswordResetService;
import poly.edu.dao.UserDAO;

import java.util.Optional;

@Controller
public class ForgotPasswordController {

    @Autowired
    private PasswordResetService resetService;

    @Autowired
    private UserDAO userDAO;

    private static final String SESSION_USER_ID = "reset_user_id";
    private static final String SESSION_OTP = "reset_otp_code";

    // =========================================================================
    // STEP 1: YÊU CẦU EMAIL
    // =========================================================================
    @GetMapping("/forgot-password")
    public String showRequestEmailForm(HttpSession session) {
        session.removeAttribute(SESSION_USER_ID);
        session.removeAttribute(SESSION_OTP);
        return "forgot/request-email";
    }

    @PostMapping("/forgot-password/request")
    public String processRequestEmail(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (email == null || email.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập Email.");
            return "redirect:/forgot-password";
        }

        Optional<User> optionalUser = resetService.createAndSendOtp(email.trim());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // 👉 Lưu userId thay vì lưu cả User entity
            session.setAttribute(SESSION_USER_ID, user.getUserId());
            redirectAttributes.addFlashAttribute("message", "Mã OTP đã được gửi đến email của bạn.");

            return "redirect:/forgot-password/verify-otp";
        }

        redirectAttributes.addFlashAttribute("error", "Email không tồn tại trong hệ thống!");
        return "redirect:/forgot-password";
    }

    // =========================================================================
    // STEP 2: XÁC THỰC OTP
    // =========================================================================
    @GetMapping("/forgot-password/verify-otp")
    public String showVerifyOtpForm(HttpSession session, RedirectAttributes redirectAttributes) {

        if (session.getAttribute(SESSION_USER_ID) == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên làm việc đã hết hạn. Vui lòng bắt đầu lại!");
            return "redirect:/forgot-password";
        }

        return "forgot/verify-otp";
    }

    @PostMapping("/forgot-password/verify")
    public String processVerifyOtp(
            @RequestParam("otp") String otpCode,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên làm việc đã hết hạn!");
            return "redirect:/forgot-password";
        }

        User user = userDAO.findById(userId).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống. Vui lòng bắt đầu lại!");
            return "redirect:/forgot-password";
        }

        if (resetService.validateOtp(user, otpCode.trim())) {
            session.setAttribute(SESSION_OTP, otpCode.trim());
            return "redirect:/forgot-password/change-password";
        }

        redirectAttributes.addFlashAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn!");
        return "redirect:/forgot-password/verify-otp";
    }

    // =========================================================================
    // STEP 3: ĐỔI MẬT KHẨU
    // =========================================================================
    @GetMapping("/forgot-password/change-password")
    public String showChangePasswordForm(HttpSession session, RedirectAttributes redirectAttributes) {

        if (session.getAttribute(SESSION_USER_ID) == null ||
                session.getAttribute(SESSION_OTP) == null) {

            redirectAttributes.addFlashAttribute("error", "Phiên làm việc không hợp lệ!");
            return "redirect:/forgot-password";
        }

        return "forgot/change-password";
    }

    @PostMapping("/forgot-password/change")
    public String processChangePassword(
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        String otpCode = (String) session.getAttribute(SESSION_OTP);

        if (userId == null || otpCode == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên làm việc không hợp lệ!");
            return "redirect:/forgot-password";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/forgot-password/change-password";
        }

        User user = userDAO.findById(userId).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống!");
            return "redirect:/forgot-password";
        }

        try {
            resetService.resetPassword(user, password, otpCode);
            session.removeAttribute(SESSION_USER_ID);
            session.removeAttribute(SESSION_OTP);

            redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công. Vui lòng đăng nhập!");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password/change-password";
        }
    }
}
