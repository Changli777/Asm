package poly.edu.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.UserDAO;
import poly.edu.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class UserDAOImpl implements UserDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :v", User.class);
        q.setParameter("v", username);
        return q.getResultStream().findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.email = :v", User.class);
        q.setParameter("v", email);
        return q.getResultStream().findFirst();
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.phone = :v", User.class);
        q.setParameter("v", phone);
        return q.getResultStream().findFirst();
    }

    @Override
    public Optional<User> findByUsernameOrEmailOrPhone(String username, String email, String phone) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :v OR u.email = :v OR u.phone = :v", User.class);
        q.setParameter("v", username); // same value for all three
        return q.getResultStream().findFirst();
    }

    @Override
    public List<User> findAll() {
        TypedQuery<User> q = em.createQuery("SELECT u FROM User u", User.class);
        return q.getResultList();
    }

    @Override
    public User add(User user) {
        if (user.getUsername() != null && existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (user.getEmail() != null && existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        if (user.getPhone() != null && findByPhone(user.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone already exists: " + user.getPhone());
        }

        em.persist(user);
        return user;
    }

    @Override
    public User save(User user) {
        if (user.getUserId() == null) {
            em.persist(user);
            return user;
        } else {
            return em.merge(user);
        }
    }

    @Override
    public void deleteById(Long id) {
        findById(id).ifPresent(u -> {
            em.remove(em.contains(u) ? u : em.merge(u));
        });
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.username = :v", Long.class)
                .setParameter("v", username)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.email = :v", Long.class)
                .setParameter("v", email)
                .getSingleResult();
        return count != null && count > 0;
    }
}
