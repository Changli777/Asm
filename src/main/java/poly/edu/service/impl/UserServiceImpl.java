package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.UserDAO;
import poly.edu.entity.User;
import poly.edu.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDAO dao;

    @Override
    public List<User> findAll() {
        return dao.findAll();
    }

    @Override
    public User findById(Long id) {
        return dao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id = " + id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return dao.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return dao.findByEmail(email);
    }

    @Override
    public User create(User user) {
        return dao.save(user);
    }

    @Override
    public User update(User user) {
        if (user.getUserId() == null || !dao.existsById(user.getUserId())) {
            throw new IllegalArgumentException("Cannot update - user not found with id = " + user.getUserId());
        }
        return dao.save(user);
    }

    @Override
    public void deleteById(Long id) {
        if (!dao.existsById(id)) {
            throw new IllegalArgumentException("Cannot delete - user not found with id = " + id);
        }
        dao.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return dao.existsById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return dao.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return dao.existsByEmail(email);
    }
}
