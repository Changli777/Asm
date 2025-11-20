package poly.edu.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.RoleDAO;
import poly.edu.dao.UserDAO;
import poly.edu.dao.UserRoleDAO;
import poly.edu.entity.Role;
import poly.edu.entity.User;
import poly.edu.entity.UserRole;

@Controller
public class RegisterController {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private UserRoleDAO userRoleDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("confirmPasswordError", "Mật khẩu xác nhận không khớp.");
            return "register";
        }

        if (userDAO.existsByUsername(user.getUsername())) {
            bindingResult.rejectValue("username", "error.username", "Username đã tồn tại.");
            return "register";
        }

        if (userDAO.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Email đã tồn tại.");
            return "register";
        }

        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Bắt buộc provider = LOCAL
        user.setProvider("LOCAL");

        // Lưu user trước
        userDAO.save(user);

        // 🚀 Gán ROLE_USER mặc định
        Role roleUser = roleDAO.findById("USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found in DB"));

        UserRole ur = UserRole.builder()
                .user(user)
                .role(roleUser)
                .build();

        userRoleDAO.save(ur);

        model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");

        return "login"; // redirect:/login cũng được
    }
}
