package poly.edu.dao;

import poly.edu.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByUsernameOrEmailOrPhone(String username, String email, String phone);
    List<User> findAll();
    User add(User user);
    User save(User user);
    void deleteById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long count();
}
