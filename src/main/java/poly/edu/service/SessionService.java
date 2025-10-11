package poly.edu.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    @jakarta.annotation.Resource
    private HttpSession session;

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        if (name == null) return null;
        return (T) session.getAttribute(name);
    }

    public void set(String name, Object value) {
        if (name == null) return;
        session.setAttribute(name, value);
    }

    public void remove(String name) {
        if (name == null) return;
        session.removeAttribute(name);
    }
}
