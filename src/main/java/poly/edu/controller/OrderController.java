package poly.edu.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.*;
import poly.edu.service.OrderDetailService;
import poly.edu.service.OrderService;
import poly.edu.service.SessionService;

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
    private SessionService sessionService;

    // ============ TRANG TRẠNG THÁI ORDERS ============
    @GetMapping("/orders")
    public String viewOrders(
            @RequestParam(defaultValue = "pending") String status,
            @RequestParam(defaultValue = "newest") String sort,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra đăng nhập
        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để xem đơn hàng.");
            return "redirect:/login";
        }

        // Lấy danh sách đơn hàng theo status
        List<Order> orders = orderService.findByStatusAndUser(status, currentUser, sort);

        // Format tiền tệ trước khi gửi sang view
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");

        for (Order order : orders) {
            // Format tổng tiền
            order.setFormattedTotalAmount(formatter.format(order.getTotal()));

            // Format detail
            for (OrderDetail detail : order.getDetails()) {
                detail.setFormattedPrice(formatter.format(detail.getPrice()));
                detail.setFormattedSubtotal(formatter.format(detail.getSubtotal()));
            }
        }

        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        model.addAttribute("currentUser", currentUser);

        // Nếu trạng thái là cancelled thì map sang cancelled-orders.html
        if ("cancelled".equalsIgnoreCase(status)) {
            return "orders/cancelled-orders";
        } else if ("shipping".equalsIgnoreCase(status)) {
            return "orders/shipping-orders";
        } else if ("delivered".equalsIgnoreCase(status)) {
            return "orders/delivered-orders";
        } else if ("confirmed".equalsIgnoreCase(status)) {
            return "orders/confirmed-orders";
        }

        // Mặc định là pending-orders.html
        return "orders/pending-orders";
    }


    // ============ TRANG CHI TIẾT ĐƠN HÀNG ============
    @GetMapping("/detail/{orderId}")
    public String viewOrderDetail(@PathVariable Long orderId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        // Kiểm tra đăng nhập
        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập để xem đơn hàng.");
            return "redirect:/login";
        }

        // Tìm đơn hàng
        Optional<Order> optionalOrder = orderService.findByOrderIdAndUser(orderId, currentUser);
        if (optionalOrder.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/orders?status=pending";
        }

        Order order = optionalOrder.get();
        List<OrderDetail> orderDetails = orderDetailService.findByOrder(order);

        // Format tiền tệ trước khi gửi sang view
        DecimalFormat formatter = new DecimalFormat("#,###");

        // Format tổng tiền
        order.setFormattedTotalAmount(formatter.format(order.getTotal()));

        // Format detail
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
    public String cancelOrder(@PathVariable Long orderId,
                              @RequestParam String cancellationReason,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {

        // Kiểm tra đăng nhập
        User currentUser = sessionService.get("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn phải đăng nhập.");
            return "redirect:/login";
        }

        // Validate lý do hủy
        if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập lý do hủy đơn.");
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/orders?status=pending");
        }

        // Hủy đơn hàng
        boolean success = orderService.cancelOrder(orderId, currentUser, cancellationReason.trim());

        if (success) {
            redirectAttributes.addFlashAttribute("message", "Đã hủy đơn hàng thành công.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy đơn hàng. Chỉ có thể hủy đơn hàng đang chờ xác nhận.");
        }

        // Redirect về trang trước
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/orders?status=pending");
    }


    @PostMapping("/orders/confirm-received")
    public String confirmReceived(@RequestParam("orderId") Long orderId, RedirectAttributes redirectAttributes) {
        // Lấy order từ DB
        Order order = orderService.findById(orderId);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại.");
            return "redirect:/orders";
        }

        // Đánh dấu khách hàng đã xác nhận
        order.setCustomerConfirmed(true);

        // Chỉ cập nhật trạng thái sang Completed khi admin_confirmed = true và customer_confirmed = true
        if (Boolean.TRUE.equals(order.getAdminConfirmed()) && Boolean.TRUE.equals(order.getCustomerConfirmed())) {
            order.setStatus("Completed");
        }
        orderService.save(order);
        redirectAttributes.addFlashAttribute("success", "Đơn hàng " + order.getOrderNumber() + " đã được xác nhận.");
        return "redirect:/orders";
    }

}