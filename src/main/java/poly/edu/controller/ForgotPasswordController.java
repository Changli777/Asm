package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.User;
import poly.edu.service.PasswordResetService;
import poly.edu.service.SessionService;
import poly.edu.dao.UserDAO;

import java.util.Optional;

@Controller
public class ForgotPasswordController {

    @Autowired
    private PasswordResetService resetService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private SessionService sessionService;

    private static final String RESET_USER_SESSION = "resetUser";
    private static final String RESET_OTP_SESSION = "resetOtp";

    // =========================================================================
    // STEP 1: YÊU C?U EMAIL
    // =========================================================================
    @GetMapping("/forgot-password")
    public String showRequestEmailForm(Model model) {
        sessionService.remove(RESET_USER_SESSION); // Xóa session c?
        sessionService.remove(RESET_OTP_SESSION);
        return "forgot/request-email";
    }

    @PostMapping("/forgot-password/request")
    public String processRequestEmail(@RequestParam("email") String email,
                                      RedirectAttributes redirectAttributes) {
        if (email == null || email.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập Email.");
            return "redirect:/forgot-password";
        }

        Optional<User> optionalUser = resetService.createAndSendOtp(email.trim());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // L?u user vào session t?m th?i
            sessionService.set(RESET_USER_SESSION, user);
            redirectAttributes.addFlashAttribute("message", "Mã OTP đã được gửi đến email của bạn.");
            return "redirect:/forgot-password/verify-otp";
        } else {
            redirectAttributes.addFlashAttribute("error", "Email không tồn tại trong hệ thống !.");
            return "redirect:/forgot-password";
        }
    }

    // =========================================================================
    // STEP 2: XÁC TH?C OTP
    // =========================================================================
    @GetMapping("/forgot-password/verify-otp")
    public String showVerifyOtpForm(Model model, RedirectAttributes redirectAttributes) {
        if (sessionService.get(RESET_USER_SESSION) == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên làm việc đã hết hạn. Vui lòng bắt đầu lại !");
            return "redirect:/forgot-password";
        }
        return "forgot/verify-otp";
    }

    @PostMapping("/forgot-password/verify")
    public String processVerifyOtp(@RequestParam("otp") String otpCode,
                                   RedirectAttributes redirectAttributes) {
        // C?p nh?t user t? DB vì user trong session ch? là detached entity
        User sessionUser = sessionService.get(RESET_USER_SESSION);
        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên làm việc đã hết hạn. Vui lòng bắt đầu lại !");
            return "redirect:/forgot-password";
        }
        User user = userDAO.findById(sessionUser.getUserId()).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống. Vui lòng bắt đầu lại !");
            return "redirect:/forgot-password";
        }

        if (resetService.validateOtp(user, otpCode.trim())) {
            // L?u mã OTP vào session ?? s? d?ng khi d?i m?t kh?u
            sessionService.set(RESET_OTP_SESSION, otpCode.trim());
            return "redirect:/forgot-password/change-password";
        } else {
            redirectAttributes.addFlashAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn !");
            return "redirect:/forgot-password/verify-otp";
        }
    }

    // =========================================================================
    // STEP 3: ??I M?T KH?U
    // =========================================================================
    @GetMapping("/forgot-password/change-password")
    public String showChangePasswordForm(RedirectAttributes redirectAttributes) {
        if (sessionService.get(RESET_USER_SESSION) == null || sessionService.get(RESET_OTP_SESSION) == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên làm việc không hợp lệ. Vui lòng bắt đầu lại !");
            return "redirect:/forgot-password";
        }
        return "forgot/change-password";
    }

    @PostMapping("/forgot-password/change")
    public String processChangePassword(@RequestParam("password") String password,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        RedirectAttributes redirectAttributes) {
        User sessionUser = sessionService.get(RESET_USER_SESSION);
        String otpCode = sessionService.get(RESET_OTP_SESSION);

        if (sessionUser == null || otpCode == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên làm việc không hợp lệ. Vui lòng bắt đầu lại !");
            return "redirect:/forgot-password";
        }

        // C?p nh?t user t? DB ?? d?m b?o là managed entity
        User user = userDAO.findById(sessionUser.getUserId()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống. Vui lòng bắt đầu lại !");
            return "redirect:/forgot-password";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp !");
            return "redirect:/forgot-password/change-password";
        }

        try {
            resetService.resetPassword(user, password, otpCode);
            sessionService.remove(RESET_USER_SESSION);
            sessionService.remove(RESET_OTP_SESSION);
            redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công. Vui lòng đăng nhập !");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password/change-password";
        }
    }
}