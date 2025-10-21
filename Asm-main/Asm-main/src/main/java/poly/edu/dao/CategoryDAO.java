package poly.edu.dao;

import poly.edu.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryDAO {

    List<Category> findAll();

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    Category save(Category category);

    void deleteById(Long id);

    boolean existsById(Long id);

    long count();
}