package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.User;
import poly.edu.entity.UserRole;

public interface UserRoleDAO extends JpaRepository<UserRole, Integer> {
    void deleteAllByUser(User user);
}
