package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poly.edu.entity.User;

import java.util.Optional;

@Repository
public interface UserDAO extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);


    @Query("SELECT u FROM User u WHERE u.username = ?1 OR u.email = ?2 OR u.phone = ?3")
    Optional<User> findByUsernameOrEmailOrPhone(String username, String email, String phone);


}
