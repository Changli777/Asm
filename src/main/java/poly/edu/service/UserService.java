package poly.edu.service;

import poly.edu.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    User findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User create(User user);

    User update(User user);

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
