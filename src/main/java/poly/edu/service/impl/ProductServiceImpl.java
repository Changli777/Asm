package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.ProductDAO;
import poly.edu.entity.Product;
import poly.edu.service.ProductService;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDAO dao;

    @Override
    public List<Product> findAll() {
        return dao.findAll();
    }

    @Override
    public Product findById(Long id) {
        return dao.findById(id).orElse(null);
    }

    @Override
    public Product create(Product entity) {
        return dao.save(entity);
    }

    @Override
    public Product update(Product entity) {
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
