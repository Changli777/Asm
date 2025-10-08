package poly.edu.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.ProductDAO;
import poly.edu.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ProductDAOImpl implements ProductDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> findAll() {
        String jpql = "SELECT p FROM Product p ORDER BY p.createdAt DESC";
        TypedQuery<Product> query = entityManager.createQuery(jpql, Product.class);
        return query.getResultList();
    }

    @Override
    public Optional<Product> findById(Long id) {
        Product product = entityManager.find(Product.class, id);
        return Optional.ofNullable(product);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        String jpql = "SELECT p FROM Product p WHERE p.categoryId = :categoryId ORDER BY p.createdAt DESC";
        TypedQuery<Product> query = entityManager.createQuery(jpql, Product.class);
        query.setParameter("categoryId", categoryId);
        return query.getResultList();
    }

    @Override
    public List<Product> findByNameContaining(String keyword) {
        String jpql = "SELECT p FROM Product p WHERE p.productName LIKE :keyword ORDER BY p.createdAt DESC";
        TypedQuery<Product> query = entityManager.createQuery(jpql, Product.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }

    @Override
    public List<Product> findNewProducts(int limit) {
        String jpql = "SELECT p FROM Product p WHERE p.isNew = true ORDER BY p.createdAt DESC";
        TypedQuery<Product> query = entityManager.createQuery(jpql, Product.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public List<Product> findSaleProducts(int limit) {
        String jpql = "SELECT p FROM Product p WHERE p.isOnSale = true ORDER BY p.createdAt DESC";
        TypedQuery<Product> query = entityManager.createQuery(jpql, Product.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public List<Product> findFeaturedProducts(int limit) {
        String jpql = "SELECT p FROM Product p WHERE p.isFeatured = true ORDER BY p.soldCount DESC";
        TypedQuery<Product> query = entityManager.createQuery(jpql, Product.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        String jpql = "SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC";
        TypedQuery<Product> query = entityManager.createQuery(jpql, Product.class);
        query.setParameter("minPrice", minPrice);
        query.setParameter("maxPrice", maxPrice);
        return query.getResultList();
    }

    @Override
    public List<Product> searchProducts(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");

        if (keyword != null && !keyword.isEmpty()) {
            jpql.append(" AND p.productName LIKE :keyword");
        }

        if (categoryId != null) {
            jpql.append(" AND p.categoryId = :categoryId");
        }

        if (minPrice != null && maxPrice != null) {
            jpql.append(" AND p.price BETWEEN :minPrice AND :maxPrice");
        }

        jpql.append(" ORDER BY p.createdAt DESC");

        TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);

        if (keyword != null && !keyword.isEmpty()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }

        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }

        if (minPrice != null && maxPrice != null) {
            query.setParameter("minPrice", minPrice);
            query.setParameter("maxPrice", maxPrice);
        }

        return query.getResultList();
    }

    @Override
    public Product save(Product product) {
        if (product.getProductId() == null) {
            // Insert
            entityManager.persist(product);
            return product;
        } else {
            // Update
            return entityManager.merge(product);
        }
    }

    @Override
    public void deleteById(Long id) {
        Product product = entityManager.find(Product.class, id);
        if (product != null) {
            entityManager.remove(product);
        }
    }

    @Override
    public boolean existsById(Long id) {
        String jpql = "SELECT COUNT(p) FROM Product p WHERE p.productId = :id";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }

    @Override
    public long count() {
        String jpql = "SELECT COUNT(p) FROM Product p";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        return query.getSingleResult();
    }

    @Override
    public void incrementViewCount(Long productId) {
        String jpql = "UPDATE Product p SET p.viewsCount = p.viewsCount + 1 WHERE p.productId = :id";
        entityManager.createQuery(jpql)
                .setParameter("id", productId)
                .executeUpdate();
    }

    @Override
    public void incrementSoldCount(Long productId, int quantity) {
        String jpql = "UPDATE Product p SET p.soldCount = p.soldCount + :quantity WHERE p.productId = :id";
        entityManager.createQuery(jpql)
                .setParameter("id", productId)
                .setParameter("quantity", quantity)
                .executeUpdate();
    }
}