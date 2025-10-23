package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.User;
import poly.edu.service.SessionService;
import poly.edu.service.UserService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Controller
public class AccountController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;

    @GetMapping("/account")
    public String accountPage(Model model) {
        User currentUser = (User) sessionService.get("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        return "user/account";
    }

    // Hiển thị form chỉnh sửa
    @GetMapping("/account/edit")
    public String editAccount(Model model) {
        User currentUser = (User) sessionService.get("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        return "user/edit"; // edit.html nằm trong templates/user/
    }

    // Xử lý submit form chỉnh sửa
    @PostMapping("/account/edit")
    public String updateAccount(@ModelAttribute("user") User formUser,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) sessionService.get("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Cập nhật các field có thể thay đổi
        currentUser.setFullName(formUser.getFullName());
        currentUser.setUsername(formUser.getUsername());
        currentUser.setEmail(formUser.getEmail());
        currentUser.setGender(formUser.getGender());
        currentUser.setDateOfBirth(formUser.getDateOfBirth());
        currentUser.setPhone(formUser.getPhone());
        currentUser.setAddress(formUser.getAddress());
        currentUser.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Lưu vào DB
        userService.update(currentUser);

        // Cập nhật session
        sessionService.set("currentUser", currentUser);

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
        User user = sessionService.get("currentUser");
        if (user == null) {
            model.addAttribute("error", "Vui lòng đăng nhập trước khi đổi mật khẩu.");
            return "account/change-password";
        }

        if (!user.getPassword().trim().equals(currentPassword.trim())) {
            model.addAttribute("error", "Mật khẩu hiện tại không đúng.");
            return "account/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Xác nhận mật khẩu không khớp.");
            return "account/change-password";
        }

        user.setPassword(newPassword);
        userService.update(user);
        sessionService.set("user", user);

        model.addAttribute("success", "Đổi mật khẩu thành công!");
        return "account/change-password";
    }
}