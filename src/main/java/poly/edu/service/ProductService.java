package poly.edu.service;

import poly.edu.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> findAll();
    Product findById(Long id);
    Product create(Product entity);
    Product update(Product entity);
    void deleteById(Long id);
    boolean existsById(Long id);
}
