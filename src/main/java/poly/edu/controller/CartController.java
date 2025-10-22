package poly.edu.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring6.SpringTemplateEngine;
import poly.edu.dao.OrderDao;
import poly.edu.dao.OrderDetailDao;
import poly.edu.dao.UserDAO;
import poly.edu.entity.*;
import poly.edu.service.CartItemService;
import poly.edu.service.SessionService;

import java.math.BigDecimal;
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

    @Autowired
    private OrderDao orderRepo;

    @Autowired
    private OrderDetailDao detailRepo;

    @Autowired
    private UserDAO userRepo;
    private HttpServletRequest request;

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
            return "fragments/cart";
        }

        List<CartItem> cartItems = cartItemService.findAllByUser(currentUser);
        BigDecimal total = cartItemService.calculateTotal(currentUser);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "fragments/cart";
    }

    // ----------------------- XOÁ 1 SẢN PHẨM -----------------------
    @PostMapping("/cart/remove/{id}")
    public String removeItem(@PathVariable("id") Long id, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        cartItemService.deleteById(id);
        // Lấy URL trang trước (trang user vừa ở)
        return "redirect:" + (referer != null ? referer : "/home");
    }

    // ----------------------- XOÁ TẤT CẢ -----------------------
    @PostMapping("/cart/clear")
    public String clearCart(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) return "redirect:/login";
        cartItemService.deleteAllByUser(currentUser);d
        // Lấy URL trang trước
        return "redirect:" + (referer != null ? referer : "/home");
    }

    // ----------------------- CẬP NHẬT SỐ LƯỢNG -----------------------
    @PutMapping("/cart/update/{id}")
    @ResponseBody
    public Map<String, Object> updateCartItem(@PathVariable("id") Long cartItemId,
                                              @RequestParam("quantity") int quantity) {
        Map<String, Object> response = new HashMap<>();

        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Bạn phải đăng nhập để thực hiện hành động này");
            return response;
        }

        Optional<CartItem> optionalItem = cartItemService.findById(cartItemId);

        if (optionalItem.isPresent()) {
            CartItem item = optionalItem.get();

            if (quantity <= 0) {
                cartItemService.deleteById(cartItemId);
                response.put("totalItem", BigDecimal.ZERO);
            } else {
                item.setQuantity(quantity);
                cartItemService.save(item);

                BigDecimal itemPrice = item.getProduct().getDiscountPrice() != null
                        ? item.getProduct().getDiscountPrice()
                        : item.getProduct().getPrice();

                response.put("totalItem", itemPrice.multiply(BigDecimal.valueOf(quantity)));
            }
        } else {
            response.put("totalItem", BigDecimal.ZERO);
        }
        // Tính tổng cộng giỏ hàng
        BigDecimal totalCart = cartItemService.calculateTotal(currentUser);
        response.put("totalCart", totalCart);
        response.put("success", true);

        List<CartItem> cartItems = cartItemService.findAllByUser(currentUser);

        BigDecimal total = cartItems.stream()
                .map(ci -> ci.getProduct().getFinalPrice()
                        .multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return response;
    }

    // ----------------------- HIỂN THỊ TRANG THANH TOÁN -----------------------
    @GetMapping("/checkout")
    public String showCheckout(Model model, RedirectAttributes redirectAttributes) {
        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để thanh toán.");
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartItemService.findAllByUser(currentUser);
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng của bạn đang trống.");
            return "redirect:/home";
        }

        BigDecimal total = cartItems.stream()
                .map(item -> item.getProduct().getFinalPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("user", currentUser);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);

        return "fragments/checkout"; // file checkout.html bạn tạo
    }

    // ----------------------- XÁC NHẬN THANH TOÁN -----------------------
    @PostMapping("/checkout/confirm")
    public String confirmCheckout(@RequestParam String fullName,
                                  @RequestParam String phone,
                                  @RequestParam String address,
                                  @RequestParam String paymentMethod,
                                  RedirectAttributes redirectAttributes) {

        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để thanh toán.");
            return "redirect:/login";
        }

        // Cập nhật thông tin người dùng
        currentUser.setFullName(fullName);
        currentUser.setPhone(phone);
        currentUser.setAddress(address);
        userRepo.save(currentUser);

        List<CartItem> cartItems = cartItemService.findAllByUser(currentUser);
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng của bạn đang trống.");
            return "redirect:/home";
        }

        // Tính tổng tiền
        BigDecimal total = cartItems.stream()
                .map(item -> item.getProduct().getFinalPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tạo order mới
        Order order = new Order();
        order.setUser(currentUser);
        order.setTotal(total);
        order.setStatus("Pending");
        order.setOrderDate(new java.util.Date());
        orderRepo.save(order);

        // Lưu chi tiết đơn hàng
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setProductName(item.getProduct().getProductName());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getProduct().getFinalPrice());
            BigDecimal subtotal = item.getProduct().getFinalPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            detail.setSubtotal(subtotal);
            detailRepo.save(detail);
        }

        // Xóa giỏ hàng sau khi đặt
        cartItemService.deleteAllByUser(currentUser);

        redirectAttributes.addFlashAttribute("message", "Đặt hàng thành công!");
        return "fragments/thankyou";
    }
}

