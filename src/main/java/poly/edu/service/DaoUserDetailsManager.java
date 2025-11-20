package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import poly.edu.dao.UserDAO;
import poly.edu.entity.User;
import poly.edu.entity.UserRole;

import java.util.List;
import java.util.stream.Collectors;

@Primary
@Service
public class DaoUserDetailsManager implements UserDetailsService {

    @Autowired
    private UserDAO userDAO;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userDAO.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username)
                );

        // 🔥 Lấy danh sách ROLE_* từ userRoles
        List<GrantedAuthority> authorities = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(role -> new SimpleGrantedAuthority(role.getId()))  // ROLE_ADMIN, ROLE_USER
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword()) // BCrypt đã mã hóa
                .authorities(authorities)     // Spring Security nhận ROLE_ADMIN, ROLE_USER
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
