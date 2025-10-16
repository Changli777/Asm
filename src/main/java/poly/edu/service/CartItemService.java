package poly.edu.service;

import poly.edu.entity.CartItem;
import poly.edu.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CartItemService {

    List<CartItem> findAllByUser(User user);
    CartItem addToCart(User user, Long productId, int quantity);
    void removeItem(Long cartItemId);
    void clearCart(User user);
    long countDistinctByUser(User user);
    public void deleteAllByUser(User user);
    void save(CartItem item);
    void deleteById(Long id);
    public void updateQuantity(Long cartItemId, int newQuantity);
    public BigDecimal calculateTotal(User user);
    Optional<CartItem> findById(Long id);
}