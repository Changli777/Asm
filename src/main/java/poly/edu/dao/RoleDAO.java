package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.Role;

public interface RoleDAO extends JpaRepository<Role, String> {
}

