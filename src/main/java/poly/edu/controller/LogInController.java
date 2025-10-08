package poly.edu.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.UserDAO;
import poly.edu.entity.User;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Controller
public class LogInController {

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // trỏ tới file login.html trong templates
    }

    // ===== Đăng nhập tài khoản bình thường =====
    @PostMapping("/login")
    public String login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            Model model
    ) {
        Optional<User> optionalUser = userDAO.findByUsername(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getPassword().equals(password)) {
                session.setAttribute("currentUser", user);
                return "redirect:/home";
            } else {
                model.addAttribute("error", "Sai mật khẩu!");
            }
        } else {
            model.addAttribute("error", "Tài khoản không tồn tại!");
        }

        return "login";
    }

    // ===== Đăng xuất =====
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ===== Đăng nhập bằng Google =====
    @PostMapping("/login/google")
    public String loginWithGoogle(@RequestParam("credential") String idTokenString, HttpSession session, Model model)
            throws GeneralSecurityException, IOException {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance()
        ).setAudience(Collections.singletonList(
                "977445546861-a4qd0n7au67tl6cpiip1sb1fcgc0cem0.apps.googleusercontent.com"
        )).build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // Kiểm tra user đã tồn tại chưa
            Optional<User> optionalUser = userDAO.findByEmail(email);
            User user;

            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            } else {
                // Nếu chưa có thì tạo mới tài khoản Google
                user = new User();
                user.setUsername(email.split("@")[0]);
                user.setEmail(email);
                user.setFullName(name);
                user.setPassword(""); // để trống vì đăng nhập qua Google
                userDAO.add(user);
            }

            // Lưu session đăng nhập
            session.setAttribute("currentUser", user);

            return "redirect:/home";
        } else {
            model.addAttribute("error", "Đăng nhập Google thất bại!");
            return "login";
        }
    }
}
