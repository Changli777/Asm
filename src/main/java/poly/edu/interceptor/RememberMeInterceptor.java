package poly.edu.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import poly.edu.dao.UserDAO;
import poly.edu.entity.User;
import poly.edu.service.CookieService;
import poly.edu.service.SessionService;

import java.util.Optional;

@Component
public class RememberMeInterceptor implements HandlerInterceptor {

    @Autowired
    private CookieService cookieService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserDAO userDAO;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Nếu session chưa có currentUser
        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) {
            // Kiểm tra cookie "user"
            String username = cookieService.getValue("user");
            if (username != null && !username.isBlank()) {
                // Tìm user trong DB theo username
                Optional<User> optionalUser = userDAO.findByUsername(username);
                optionalUser.ifPresent(user -> sessionService.set("currentUser", user));
            }
        }

        return true; // tiếp tục request
    }
}
