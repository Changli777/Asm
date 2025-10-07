package poly.edu.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.dao.UserDAO;
import poly.edu.entity.User;

@Controller
public class RegisterController {

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @ModelAttribute("user") User user,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            Model model
    ) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            model.addAttribute("error", "Tên đăng nhập không được để trống.");
            return "register";
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            model.addAttribute("error", "Mật khẩu không được để trống.");
            return "register";
        }

        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
            return "register";
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            model.addAttribute("error", "Email không được để trống.");
            return "register";
        }

        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            model.addAttribute("error", "Số điện thoại không được để trống.");
            return "register";
        }

        // ✅ 2. Kiểm tra trùng lặp
        if (userDAO.findByUsernameOrEmailOrPhone(user.getUsername(), user.getEmail(), user.getPhone()).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập, email hoặc số điện thoại đã tồn tại.");
            return "register";
        }

        User savedUser = userDAO.save(user);
        session.setAttribute("currentUser", savedUser);

        return "redirect:/home";
    }
}
