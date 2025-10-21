package poly.edu.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.OrderDAO;
import poly.edu.entity.Order;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class OrderDAOImpl implements OrderDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Order> findAll() {
        String jpql = "SELECT o FROM Order o ORDER BY o.createdAt DESC";
        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class);
        return query.getResultList();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Order.class, id));
    }

    @Override
    public Order save(Order order) {
        if (order.getOrderId() == null) {
            entityManager.persist(order);
            return order;
        } else {
            return entityManager.merge(order);
        }
    }

    @Override
    public void deleteById(Long id) {
        Order order = entityManager.find(Order.class, id);
        if (order != null) {
            entityManager.remove(order);
        }
    }

    @Override
    public Double getTotalRevenue() {
        String jpql = "SELECT SUM(CAST(o.totalAmount AS double)) FROM Order o";
        Double total = entityManager.createQuery(jpql, Double.class).getSingleResult();
        return total != null ? total : 0.0;
    }

    @Override
    public long count() {
        String jpql = "SELECT COUNT(o) FROM Order o";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        return query.getSingleResult();
    }
}
