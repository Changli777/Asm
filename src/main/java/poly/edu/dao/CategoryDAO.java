package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryDAO extends JpaRepository<Category,Long> {

    List<Category> findAll();

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    Category save(Category category);

    void deleteById(Long id);

    boolean existsById(Long id);

    long count();
}