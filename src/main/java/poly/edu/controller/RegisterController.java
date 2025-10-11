package poly.edu.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import poly.edu.dao.UserDAO;
import poly.edu.entity.User;
import poly.edu.service.CookieService;
import poly.edu.service.ParamService;
import poly.edu.service.SessionService;

@Controller
public class RegisterController {

    //Link: http://localhost:8080/register

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ParamService paramService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CookieService cookieService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            Model model
    ) {
        String confirmPassword = paramService.getString("confirmPassword", "");

        if (bindingResult.hasErrors()) {
            model.addAttribute("confirmPasswordError", "");
            return "register";
        }

        // kiểm tra confirm password -> hiển thị bên dưới ô confirmPassword
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("confirmPasswordError", "Mật khẩu xác nhận không khớp.");
            return "register";
        }

        // ✅ 2. Kiểm tra trùng lặp chi tiết: username/email/phone
        if (userDAO.findByUsername(user.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "error.username", "Username đã tồn tại.");
            return "register";
        }
        if (userDAO.findByEmail(user.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "error.email", "Email đã tồn tại.");
            return "register";
        }
        if (user.getPhone() != null && !user.getPhone().isBlank() && userDAO.findByPhone(user.getPhone()).isPresent()) {
            bindingResult.rejectValue("phone", "error.phone", "Số điện thoại đã tồn tại.");
            return "register";
        }

        User savedUser = userDAO.save(user);

        sessionService.set("currentUser", savedUser);

        try {
            cookieService.add("username", savedUser.getUsername(), 24);
        } catch (Exception ignored) {}

        return "redirect:/home";
    }
}
