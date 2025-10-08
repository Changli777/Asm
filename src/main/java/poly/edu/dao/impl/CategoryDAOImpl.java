package poly.edu.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.CategoryDAO;
import poly.edu.entity.Category;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CategoryDAOImpl implements CategoryDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Category> findAll() {
        String jpql = "SELECT c FROM Category c ORDER BY c.categoryName ASC";
        TypedQuery<Category> query = entityManager.createQuery(jpql, Category.class);
        return query.getResultList();
    }

    @Override
    public Optional<Category> findById(Long id) {
        Category category = entityManager.find(Category.class, id);
        return Optional.ofNullable(category);
    }

    @Override
    public Optional<Category> findByName(String name) {
        String jpql = "SELECT c FROM Category c WHERE c.categoryName = :name";
        TypedQuery<Category> query = entityManager.createQuery(jpql, Category.class);
        query.setParameter("name", name);
        try {
            Category category = query.getSingleResult();
            return Optional.of(category);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Category save(Category category) {
        if (category.getCategoryId() == null) {
            // Insert
            entityManager.persist(category);
            return category;
        } else {
            // Update
            return entityManager.merge(category);
        }
    }

    @Override
    public void deleteById(Long id) {
        Category category = entityManager.find(Category.class, id);
        if (category != null) {
            entityManager.remove(category);
        }
    }

    @Override
    public boolean existsById(Long id) {
        String jpql = "SELECT COUNT(c) FROM Category c WHERE c.categoryId = :id";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }

    @Override
    public long count() {
        String jpql = "SELECT COUNT(c) FROM Category c";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        return query.getSingleResult();
    }
}