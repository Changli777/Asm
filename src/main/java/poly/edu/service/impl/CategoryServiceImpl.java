package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.CategoryDAO;
import poly.edu.entity.Category;
import poly.edu.service.CategoryService;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryDAO dao;

    @Override
    public List<Category> findAll() {
        return dao.findAll();
    }

    @Override
    public Category findById(Long id) {
        return dao.findById(id).get();
    }

    @Override
    public Category create(Category entity) {
        return dao.save(entity);
    }

    @Override
    public Category update(Category entity) {
        return dao.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        dao.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return dao.existsById(id);
    }
}
