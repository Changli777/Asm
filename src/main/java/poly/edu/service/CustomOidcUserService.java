package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import poly.edu.dao.RoleDAO;
import poly.edu.dao.UserDAO;
import poly.edu.dao.UserRoleDAO;
import poly.edu.entity.Role;
import poly.edu.entity.User;
import poly.edu.entity.UserRole;

import java.util.*;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private UserRoleDAO userRoleDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // delegate mặc định để lấy OidcUser (idToken + userInfo)
        OidcUser oidcUser = super.loadUser(userRequest);

        Map<String, Object> attributes = oidcUser.getClaims();
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String fullName = (String) attributes.get("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("OIDC provider did not return email");
        }

        // TẠO hoặc CẬP NHẬT user local
        Optional<User> existingUser = userDAO.findByEmail(email);
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setProvider(provider.toUpperCase());
            user.setProviderId(providerId);
            if (user.getGender() == null) {
                user.setGender(false);
            }
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
            user.setGender(false); // tránh lỗi NOT NULL
            userDAO.save(user);

            // gán ROLE_USER mặc định (nếu DB chưa có thì sẽ ném - đảm bảo roles đã seed)
            Role roleUser = roleDAO.findById("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found in DB"));
            UserRole ur = UserRole.builder().user(user).role(roleUser).build();
            userRoleDAO.save(ur);
        }

        // --- LẤY ROLE THỰC SỰ TỪ DB VÀ MAPPING SANG GrantedAuthority ---
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        // Lấy userRoles từ DB (dùng method findByUser)
        List<UserRole> userRoles = userRoleDAO.findByUser(user);
        if (userRoles != null && !userRoles.isEmpty()) {
            for (UserRole ur : userRoles) {
                Role r = ur.getRole();
                if (r != null) {
                    // r.getId() giả sử là "ROLE_USER" hoặc "ROLE_ADMIN"
                    mappedAuthorities.add(new SimpleGrantedAuthority(r.getId()));
                }
            }
        } else {
            // fallback: ít nhất đảm bảo ROLE_USER để UI hiển thị đúng
            mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // Giữ thêm các authority gốc từ oidcUser nếu muốn
        oidcUser.getAuthorities().forEach(a -> mappedAuthorities.add(a));

        // --- CHỈNH SỬA: thêm username vào claims và trả DefaultOidcUser với nameAttributeKey = "username" ---
        // Copy claims gốc, thêm username local
        Map<String, Object> newClaims = new HashMap<>(oidcUser.getClaims());
        newClaims.put("username", user.getUsername());

        // Tạo OidcUserInfo mới từ claims đã chỉnh
        OidcUserInfo newUserInfo = new OidcUserInfo(newClaims);

        // Chọn nameAttributeKey là "username" để Authentication.getName() trả username
        String nameAttributeKey = "username";

        // Trả về DefaultOidcUser với mapped authorities, idToken và userInfo đã chỉnh
        return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), newUserInfo, nameAttributeKey);
    }
}
