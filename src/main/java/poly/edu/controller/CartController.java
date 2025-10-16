package poly.edu.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring6.SpringTemplateEngine;
import poly.edu.entity.CartItem;
import poly.edu.entity.User;
import poly.edu.service.CartItemService;
import poly.edu.service.SessionService;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class CartController {

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    // ----------------------- THÊM VÀO GIỎ -----------------------
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để thực hiện hành động này");
            return "redirect:/login";
        }

        try {
            cartItemService.addToCart(currentUser, productId, 1);
            redirectAttributes.addFlashAttribute("message", "Đã thêm sản phẩm vào giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm sản phẩm vào giỏ hàng!");
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    // ----------------------- ĐẾM SẢN PHẨM -----------------------
    @GetMapping("/cart/count")
    @ResponseBody
    public long getCartCount() {
        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) return 0;
        return cartItemService.countDistinctByUser(currentUser);
    }

    // ----------------------- HIỂN THỊ GIỎ HÀNG -----------------------
    @GetMapping("/cart")
    public String viewCart(Model model) {
        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) {
            model.addAttribute("cartItems", List.of());
            model.addAttribute("total", BigDecimal.ZERO);
            return "fragments/cart"; // file thymeleaf fragment
        }

        List<CartItem> cartItems = cartItemService.findAllByUser(currentUser);

        BigDecimal total = cartItems.stream()
                .map(item -> item.getProduct().getFinalPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "fragments/cart";
    }

    // ----------------------- XOÁ 1 SẢN PHẨM -----------------------
    @DeleteMapping("/cart/remove/{id}")
    @ResponseBody
    public ResponseEntity<?> removeItem(@PathVariable("id") Long id) {
        try {
            cartItemService.deleteById(id);
            return ResponseEntity.ok("removed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("error");
        }
    }

    // ----------------------- XOÁ TẤT CẢ -----------------------
    @DeleteMapping("/cart/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart() {
        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) return ResponseEntity.badRequest().body("not_logged_in");
        cartItemService.deleteAllByUser(currentUser);
        return ResponseEntity.ok("cleared");
    }

    // ----------------------- CẬP NHẬT SỐ LƯỢNG -----------------------
    @PutMapping("/cart/update/{id}")
    public String updateCartItem(@PathVariable("id") Long cartItemId,
                                 @RequestParam("quantity") int quantity,
                                 Model model) {
        // Lấy user đang đăng nhập từ session
        User currentUser = sessionService.get("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        // Tìm sản phẩm trong giỏ của user
        Optional<CartItem> optionalItem = cartItemService.findById(cartItemId);

        if (optionalItem.isPresent()) {
            CartItem item = optionalItem.get();

            if (quantity <= 0) {
                cartItemService.deleteById(cartItemId);
            } else {
                item.setQuantity(quantity);
                cartItemService.save(item);
            }
        }

        // Lấy lại danh sách giỏ hàng sau khi cập nhật
        List<CartItem> cartItems = cartItemService.findAllByUser(currentUser);

        // Tính lại tổng tiền
        BigDecimal total = cartItems.stream()
                .map(ci -> ci.getProduct().getFinalPrice()
                        .multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);

        // Trả về fragment để cập nhật giao diện ngay lập tức
        return "fragments/cart :: cartPanel";
    }
}