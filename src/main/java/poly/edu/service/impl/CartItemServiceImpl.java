package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.CartItemDAO;
import poly.edu.dao.ProductDAO;
import poly.edu.entity.CartItem;
import poly.edu.entity.Product;
import poly.edu.entity.User;
import poly.edu.service.CartItemService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    private CartItemDAO cartItemDAO;

    @Autowired
    private ProductDAO productDAO;

    @Override
    public List<CartItem> findAllByUser(User user) {
        return cartItemDAO.findByUser(user);
    }

    @Override
    public CartItem addToCart(User user, Long productId, int quantity) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm có id = " + productId));

        // tìm item đã tồn tại
        Optional<CartItem> existingItemOpt = cartItemDAO.findByUserAndProduct(user, product);

        CartItem item;
        if (existingItemOpt.isPresent()) {
            item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            item = new CartItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUser(user);
        }
        return cartItemDAO.save(item);
    }

    @Override
    public void removeItem(Long cartItemId) {
        cartItemDAO.deleteById(cartItemId);
    }

    @Override
    public void clearCart(User user) {
        List<CartItem> items = findAllByUser(user);
        cartItemDAO.deleteAll(items);
    }

    @Override
    public long countDistinctByUser(User user) {
        return cartItemDAO.countDistinctByUser(user);
    }

    @Override
    public void deleteAllByUser(User user) {
        cartItemDAO.deleteAllByUser(user);
    }

    @Override
    public void save(CartItem item) {
        cartItemDAO.save(item);
    }

    @Override
    public void deleteById(Long id) {
        cartItemDAO.deleteById(id);
    }

    @Override
    public void updateQuantity(Long cartItemId, int newQuantity) {
        CartItem item = cartItemDAO.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (newQuantity <= 0) {
            cartItemDAO.delete(item); // Xóa khi = 0
        } else {
            item.setQuantity(newQuantity);
            cartItemDAO.save(item); // Cập nhật lại
        }
    }

    @Override
    public BigDecimal calculateTotal(User user) {
        List<CartItem> cartItems = cartItemDAO.findByUser(user);
        return cartItems.stream()
                .map(ci -> ci.getProduct().getFinalPrice()
                        .multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        return cartItemDAO.findById(id);
    }
}