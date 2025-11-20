package poly.edu.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.*;
import poly.edu.service.OrderDetailService;
import poly.edu.service.OrderService;
import poly.edu.service.UserService;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    // Lấy user thông qua Spring Security
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        String username = auth.getName();
        return userService.findByUsername(username).orElse(null);
    }

    // ============ TRANG LIST ORDERS THEO STATUS ============
    @GetMapping("/orders")
    public String viewOrders(
            @RequestParam(defaultValue = "pending") String status,
            @RequestParam(defaultValue = "newest") String sort,
            Model model,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để xem đơn hàng.");
            return "redirect:/login";
        }

        List<Order> orders = orderService.findByStatusAndUser(status, currentUser, sort);

        DecimalFormat formatter = new DecimalFormat("#,###");

        for (Order order : orders) {
            order.setFormattedTotalAmount(formatter.format(order.getTotal()));

            for (OrderDetail detail : order.getDetails()) {
                detail.setFormattedPrice(formatter.format(detail.getPrice()));
                detail.setFormattedSubtotal(formatter.format(detail.getSubtotal()));
            }
        }

        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        model.addAttribute("currentUser", currentUser);

        // Trả về template theo status
        switch (status.toLowerCase()) {
            case "cancelled":
                return "orders/cancelled-orders";
            case "shipping":
                return "orders/shipping-orders";
            case "delivered":
                return "orders/delivered-orders";
            case "confirmed":
                return "orders/confirmed-orders";
            case "completed":
                return "orders/completed-orders";
            default:
                return "orders/pending-orders";
        }
    }

    // ============ TRANG CHI TIẾT ĐƠN HÀNG ============
    @GetMapping("/detail/{orderId}")
    public String viewOrderDetail(
            @PathVariable Long orderId,
            Model model,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để xem đơn hàng.");
            return "redirect:/login";
        }

        Optional<Order> optionalOrder = orderService.findByOrderIdAndUser(orderId, currentUser);
        if (optionalOrder.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/orders?status=pending";
        }

        Order order = optionalOrder.get();
        List<OrderDetail> orderDetails = orderDetailService.findByOrder(order);

        DecimalFormat formatter = new DecimalFormat("#,###");
        order.setFormattedTotalAmount(formatter.format(order.getTotal()));

        for (OrderDetail detail : orderDetails) {
            detail.setFormattedPrice(formatter.format(detail.getPrice()));
            detail.setFormattedSubtotal(formatter.format(detail.getSubtotal()));
        }

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("currentUser", currentUser);

        return "orders/order-detail";
    }

    // ============ HỦY ĐƠN HÀNG ============
    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String cancellationReason,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập.");
            return "redirect:/login";
        }

        if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập lý do hủy đơn.");
            return "redirect:" + request.getHeader("Referer");
        }

        boolean success = orderService.cancelOrder(orderId, currentUser, cancellationReason.trim());

        if (success) {
            redirectAttributes.addFlashAttribute("message", "Đã hủy đơn hàng thành công.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy đơn hàng. Chỉ có thể hủy đơn đang chờ xác nhận.");
        }

        return "redirect:" + request.getHeader("Referer");
    }

    // ============ KHÁCH XÁC NHẬN ĐÃ NHẬN HÀNG ============
    @PostMapping("/orders/confirm-received")
    public String confirmReceived(
            @RequestParam("orderId") Long orderId,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập.");
            return "redirect:/login";
        }

        Order order = orderService.findById(orderId);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại.");
            return "redirect:/orders";
        }

        // Khách xác nhận đã nhận hàng
        order.setCustomerConfirmed(true);

        if (Boolean.TRUE.equals(order.getAdminConfirmed()) &&
                Boolean.TRUE.equals(order.getCustomerConfirmed())) {

            order.setStatus("Completed");
        }

        orderService.save(order);

        redirectAttributes.addFlashAttribute("success",
                "Đơn hàng " + order.getOrderNumber() + " đã được xác nhận.");

        return "redirect:/orders";
    }
}
