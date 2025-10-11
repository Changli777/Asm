package poly.edu.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    @jakarta.annotation.Resource
    private HttpServletRequest request;

    @jakarta.annotation.Resource
    private HttpServletResponse response;

    public Cookie get(String name) {
        if (name == null) return null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c;
        }
        return null;
    }

    public String getValue(String name) {
        Cookie c = get(name);
        return c == null ? "" : c.getValue();
    }

    public Cookie add(String name, String value, int hours) {
        Cookie c = new Cookie(name, value == null ? "" : value);
        c.setPath("/"); // cho toàn site
        c.setMaxAge(hours * 3600);
        // bạn có thể set secure/httpOnly nếu cần:
        // c.setHttpOnly(true);
        response.addCookie(c);
        return c;
    }

    public void remove(String name) {
        if (name == null) return;
        Cookie c = new Cookie(name, "");
        c.setPath("/");
        c.setMaxAge(0); // xóa cookie
        response.addCookie(c);
    }
}
