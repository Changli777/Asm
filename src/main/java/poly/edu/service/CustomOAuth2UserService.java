package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import poly.edu.dao.RoleDAO;
import poly.edu.dao.UserDAO;
import poly.edu.dao.UserRoleDAO;
import poly.edu.entity.Role;
import poly.edu.entity.User;
import poly.edu.entity.UserRole;

import java.util.*;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private UserRoleDAO userRoleDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Lấy user attributes từ provider (google)
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String providerId = (String) attributes.get("sub"); // google unique id
        String email = (String) attributes.get("email");
        String fullName = (String) attributes.get("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Google account does not provide an email");
        }

        // Tạo hoặc cập nhật user local trong DB
        Optional<User> existingUser = userDAO.findByEmail(email);
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setProvider(provider.toUpperCase());
            user.setProviderId(providerId);
            if (fullName != null) user.setFullName(fullName);
            userDAO.save(user);
        } else {
            user = new User();
            String baseUsername = email.split("@")[0].replaceAll("[^A-Za-z0-9]", "");
            String username = baseUsername;
            int i = 1;
            while (userDAO.existsByUsername(username)) {
                username = baseUsername + i++;
            }
            user.setUsername(username);
            user.setEmail(email);
            user.setFullName(fullName != null ? fullName : username);
            user.setProvider(provider.toUpperCase());
            user.setProviderId(providerId);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            userDAO.save(user);

            // gán ROLE_USER (DB bạn có Id = "ROLE_USER")
            Role roleUser = roleDAO.findById("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found in DB"));
            UserRole ur = UserRole.builder()
                    .user(user)
                    .role(roleUser)
                    .build();
            userRoleDAO.save(ur);
        }

        // --- QUAN TRỌNG: build authorities để Spring Security có ROLE_USER ---
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        // Thêm role mặc định ROLE_USER (bắt buộc cho thymeleaf sec:authorize)
        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Nếu provider trả về authorities, giữ lại (tuỳ bạn)
        oauth2User.getAuthorities().forEach(a -> mappedAuthorities.add(a));

        // Chọn attribute để làm "name" principal — google có "sub" hoặc "email"
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        if (userNameAttributeName == null || userNameAttributeName.isEmpty()) {
            // fallback
            userNameAttributeName = "sub";
        }

        // Trả về DefaultOAuth2User có authorities mà ta đã map
        return new DefaultOAuth2User(mappedAuthorities, attributes, userNameAttributeName);
    }
}
