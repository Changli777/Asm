package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import poly.edu.dto.LoginForm;
import poly.edu.service.CookieService;

@Controller
public class LogInController {

    @Autowired
    private CookieService cookieService;

    @GetMapping("/login")
    public String showLoginPage(Model model) {

        LoginForm loginForm = new LoginForm();

        // Lấy username đã lưu vào cookie (nếu có)
        String remembered = cookieService.getValue("user");
        if (remembered != null && !remembered.isBlank()) {
            loginForm.setUsername(remembered);
            loginForm.setRemember(true);
        }

        model.addAttribute("loginForm", loginForm);
        return "login";
    }
}
