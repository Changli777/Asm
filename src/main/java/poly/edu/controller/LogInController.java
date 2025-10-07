package poly.edu.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.dao.UserDAO;
import poly.edu.entity.User;

import java.util.Optional;

@Controller
public class LogInController {

    //Link: http://localhost:8080/login

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Xử lý POST /login
     * - username param có thể là username/ email/ phone
     * - nếu thành công: lưu user vào session key "currentUser" -> redirect về /home.html
     * - nếu thất bại: trả về lại view login với model attribute "error"
     */
    @PostMapping("/login")
    public String doLogin(
            @RequestParam("username") String usernameOrEmailOrPhone,
            @RequestParam("password") String password,
            HttpSession session,
            Model model
    ) {

        Optional<User> optUser = userDAO.findByUsernameOrEmailOrPhone(
                usernameOrEmailOrPhone, usernameOrEmailOrPhone, usernameOrEmailOrPhone
        );

        if (optUser.isEmpty()) {
            model.addAttribute("error", "Tên tài khoản / email / số điện thoại không tồn tại.");
            return "login";
        }

        User user = optUser.get();
        String savedPassword = user.getPassword();

        if (savedPassword == null || !savedPassword.equals(password)) {
            model.addAttribute("error", "Mật khẩu không đúng.");
            return "login";
        }

        // thành công
        session.setAttribute("currentUser", user);
        return "redirect:/home";
    }

    // Đăng xuất
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
