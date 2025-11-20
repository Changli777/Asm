package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.User;
import poly.edu.service.UserService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Controller
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Lấy user từ Spring Security (KHÔNG dùng sessionService nữa)
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        String username = auth.getName();
        return userService.findByUsername(username).orElse(null);
    }

    @GetMapping("/account")
    public String accountPage(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        return "user/account";
    }

    @GetMapping("/account/edit")
    public String editAccount(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        return "user/edit";
    }

    @PostMapping("/account/edit")
    public String updateAccount(
            @ModelAttribute("user") User formUser,
            RedirectAttributes redirectAttributes
    ) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        currentUser.setFullName(formUser.getFullName());
        currentUser.setUsername(formUser.getUsername());
        currentUser.setEmail(formUser.getEmail());
        currentUser.setGender(formUser.getGender());
        currentUser.setDateOfBirth(formUser.getDateOfBirth());
        currentUser.setPhone(formUser.getPhone());
        currentUser.setAddress(formUser.getAddress());
        currentUser.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        userService.update(currentUser);

        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        return "redirect:/account";
    }

    @GetMapping("/account/change-password")
    public String changePasswordForm() {
        return "account/change-password";
    }

    @PostMapping("/account/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {
        User user = getCurrentUser();
        if (user == null) {
            model.addAttribute("error", "Vui lòng đăng nhập để đổi mật khẩu.");
            return "account/change-password";
        }

        // So sánh password cũ bằng BCrypt
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("error", "Mật khẩu hiện tại không đúng.");
            return "account/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Xác nhận mật khẩu không khớp.");
            return "account/change-password";
        }

        // Mã hóa password trước khi lưu
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.update(user);

        model.addAttribute("success", "Đổi mật khẩu thành công!");
        return "account/change-password";
    }
}
