package poly.edu.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.dao.OrderDAO;
import poly.edu.dao.OrderDetailDAO;
import poly.edu.dao.UserDAO;
import poly.edu.entity.*;
import poly.edu.service.CartItemService;
import poly.edu.service.ProductService;
import poly.edu.service.UserService;

import java.math.BigDecimal;
import java.util.*;

@Controller
public class CartController {

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserDAO userRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDAO orderDao;

    @Autowired
    private OrderDetailDAO detailDao;

    // Lấy user đang đăng nhập
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        String username = auth.getName();
        return userService.findByUsername(username).orElse(null);
    }

    // ----------------------- THÊM VÀO GIỎ -----------------------
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để thêm sản phẩm vào giỏ hàng.");
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
        User currentUser = getCurrentUser();
        if (currentUser == null) return 0;
        return cartItemService.countDistinctByUser(currentUser);
    }

    // ----------------------- HIỂN THỊ GIỎ HÀNG -----------------------
    @GetMapping("/cart")
    public String viewCart(Model model) {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            model.addAttribute("cartItems", Collections.emptyList());
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
        User currentUser = getCurrentUser();
        if (currentUser == null) return "redirect:/login";

        Optional<CartItem> itemOpt = cartItemService.findById(id);
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            if (item.getUser().getUserId().equals(currentUser.getUserId())) {
                cartItemService.deleteById(id);
            }
        }

        return "redirect:" + Optional.ofNullable(request.getHeader("Referer")).orElse("/home");
    }

    // ----------------------- XOÁ TẤT CẢ -----------------------
    @PostMapping("/cart/clear")
    public String clearCart(HttpServletRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return "redirect:/login";

        cartItemService.deleteAllByUser(currentUser);
        return "redirect:" + Optional.ofNullable(request.getHeader("Referer")).orElse("/home");
    }

    // ----------------------- CẬP NHẬT SỐ LƯỢNG -----------------------
    @PutMapping("/cart/update/{id}")
    @ResponseBody
    public Map<String, Object> updateCartItem(@PathVariable("id") Long cartItemId,
                                              @RequestParam("quantity") int quantity) {

        Map<String, Object> response = new HashMap<>();

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Bạn phải đăng nhập để thực hiện hành động này");
            return response;
        }

        Optional<CartItem> optionalItem = cartItemService.findById(cartItemId);

        if (optionalItem.isPresent()) {
            CartItem item = optionalItem.get();

            // Kiểm tra giỏ thuộc user hiện tại
            if (!item.getUser().getUserId().equals(currentUser.getUserId())) {
                response.put("success", false);
                response.put("message", "Không được chỉnh sửa giỏ hàng người khác.");
                return response;
            }

            if (quantity <= 0) {
                cartItemService.deleteById(cartItemId);
                response.put("totalItem", BigDecimal.ZERO);
            } else {
                item.setQuantity(quantity);
                cartItemService.save(item);

                BigDecimal price = item.getProduct().getFinalPrice();
                response.put("totalItem", price.multiply(BigDecimal.valueOf(quantity)));
            }
        } else {
            response.put("totalItem", BigDecimal.ZERO);
        }

        BigDecimal totalCart = cartItemService.calculateTotal(currentUser);
        response.put("totalCart", totalCart);
        response.put("success", true);

        return response;
    }

    // ----------------------- HIỂN THỊ TRANG CHECKOUT -----------------------
    @GetMapping("/checkout")
    public String showCheckout(Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để thanh toán.");
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartItemService.findAllByUser(currentUser);
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng của bạn đang trống.");
            return "redirect:/home";
        }

        BigDecimal total = cartItemService.calculateTotal(currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);

        return "fragments/checkout";
    }

    // ----------------------- XÁC NHẬN ĐẶT HÀNG -----------------------
    @PostMapping("/checkout/confirm")
    public String confirmCheckout(@RequestParam String fullName,
                                  @RequestParam String phone,
                                  @RequestParam String address,
                                  @RequestParam String paymentMethod,
                                  RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để thanh toán.");
            return "redirect:/login";
        }

        if (fullName.isBlank() || phone.isBlank() || address.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ thông tin giao hàng.");
            return "redirect:/checkout";
        }

        // Cập nhật thông tin user
        currentUser.setFullName(fullName);
        currentUser.setPhone(phone);
        currentUser.setAddress(address);
        userRepo.save(currentUser);

        List<CartItem> cartItems = cartItemService.findAllByUser(currentUser);
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng đang trống.");
            return "redirect:/home";
        }

        // Tạo ORDER
        Order order = new Order();
        order.setUser(currentUser);
        order.setStatus("Pending");
        order.setOrderDate(new Date());
        order.setCreatedAt(new Date());
        order.setShippingFullName(fullName);
        order.setShippingPhone(phone);
        order.setShippingAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("Pending");
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setOrderNumber("ORD-" + System.currentTimeMillis());

        orderDao.save(order);

        // Lưu ORDER DETAILS và cập nhật product
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
            detailDao.save(detail);

            Product product = item.getProduct();
            product.setSoldCount(
                    product.getSoldCount() == null ? item.getQuantity()
                            : product.getSoldCount() + item.getQuantity()
            );

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productService.update(product);
        }

        // Xóa giỏ hàng sau khi đặt hàng
        cartItemService.deleteAllByUser(currentUser);

        redirectAttributes.addFlashAttribute(
                "message",
                "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderNumber()
        );
        return "redirect:/home";
    }
}
