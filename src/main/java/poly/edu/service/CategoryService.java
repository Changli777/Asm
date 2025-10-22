package poly.edu.service;

import org.springframework.stereotype.Service;
import poly.edu.entity.Category;
import java.util.List;

@Service
public interface CategoryService {
    List<Category> findAll();
    Category findById(Long id);
    Category create(Category entity);
    Category update(Category entity);
    void deleteById(Long id);
    boolean existsById(Long id);
}
