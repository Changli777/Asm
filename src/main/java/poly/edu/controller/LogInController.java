package poly.edu.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.UserDAO;
import poly.edu.dto.LoginForm;
import poly.edu.entity.User;
import poly.edu.service.CookieService;
import poly.edu.service.ParamService;
import poly.edu.service.SessionService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Controller
public class LogInController {

    //Link: http://localhost:8080/login
    @Autowired
    private ParamService paramService;
    @Autowired
    private CookieService cookieService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private UserDAO userDAO;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        LoginForm loginForm = new LoginForm();

        // Nếu có cookie "user" (cookie lưu username) thì tiền điền username
        String remembered = cookieService.getValue("user");
        if (remembered != null && !remembered.isBlank()) {
            loginForm.setUsername(remembered);
            loginForm.setRemember(true);
        }
        model.addAttribute("loginForm", loginForm);
        model.addAttribute("user", new poly.edu.entity.User());
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult,
            Model model
    ) {
        // nếu có lỗi validation -> trả về trang login để hiển thị message từ DTO
        if (bindingResult.hasErrors()) {
            // đảm bảo model có attribute cần thiết cho template
            model.addAttribute("loginForm", loginForm);
            model.addAttribute("user", new poly.edu.entity.User());
            return "login";
        }

        String identifier = loginForm.getUsername().trim();
        String password = loginForm.getPassword();

        // tìm user bằng username OR email OR phone
        Optional<User> optionalUser = userDAO.findByUsernameOrEmailOrPhone(identifier, identifier, identifier);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getPassword() != null && user.getPassword().equals(password)) {
                // Lưu session: sử dụng SessionService
                sessionService.set("currentUser", user);
                // xử lý remember cookie
                if (loginForm.isRemember()) {
                    cookieService.add("user", user.getUsername(), 24);
                } else {
                    cookieService.remove("user");
                }
                return "redirect:/home";
            } else {
                model.addAttribute("error", "Sai mật khẩu!");
                model.addAttribute("loginForm", loginForm);
                model.addAttribute("user", new poly.edu.entity.User());
                return "login";
            }
        } else {
            model.addAttribute("error", "Tài khoản không tồn tại!");
            model.addAttribute("loginForm", loginForm);
            model.addAttribute("user", new poly.edu.entity.User());
            return "login";
        }
    }

    // ===== Đăng xuất =====
    @PostMapping("/logout")
    public String logout(Model model) {
        sessionService.remove("username");
        cookieService.remove("user");
        model.addAttribute("message", "Đăng xuất thành công!");
        return "redirect:/login";
    }

    // ===== Đăng nhập bằng Google =====
    @PostMapping("/login/google")
    public String loginWithGoogle(@RequestParam("credential") String idTokenString,
                                  HttpSession session, Model model)
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
            String googleId = payload.getSubject();

            // Kiểm tra user đã tồn tại chưa
            Optional<User> optionalUser = userDAO.findByEmail(email);
            User user;

            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            } else {
                // Tạo user mới nếu chưa có
                user = new User();
                user.setUsername(email.split("@")[0]);
                user.setEmail(email);
                user.setFullName(name != null ? name : email.split("@")[0]);
                user.setPassword(""); // không dùng cho Google login
                user.setGender(true); // mặc định true (hoặc false tùy bạn)
                user.setRole("USER");
                user.setProvider("GOOGLE");
                user.setProviderId(googleId);
                userDAO.add(user);
            }

            // Lưu session
            session.setAttribute("currentUser", user);
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Đăng nhập Google thất bại!");
            return "login";
        }
    }
}
