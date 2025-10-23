package poly.edu.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Order;
import poly.edu.service.OrderService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/orders")
public class CRUDOrderController {

    @Autowired
    private OrderService orderService;

    // Trang danh sách đơn hàng
    @GetMapping
    public String listOrders(@RequestParam(value = "status", required = false) String status, Model model) {
        List<Order> orders;
        if (status == null || status.isEmpty()) {
            orders = orderService.findAll();
        } else {
            orders = orderService.findByStatus(status);
        }

        // Tính tổng số đơn theo trạng thái
        Map<String, Long> counts = orderService.findAll().stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status != null ? status : "");

        model.addAttribute("totalCount", orderService.countAll());
        model.addAttribute("pendingCount", counts.getOrDefault("Pending", 0L));
        model.addAttribute("confirmedCount", counts.getOrDefault("Confirmed", 0L));
        model.addAttribute("shippingCount", counts.getOrDefault("Shipping", 0L));
        model.addAttribute("deliveredCount", counts.getOrDefault("Delivered", 0L));
        model.addAttribute("completedCount", counts.getOrDefault("Completed", 0L));
        model.addAttribute("cancelledCount", counts.getOrDefault("Cancelled", 0L));

        return "admin/orders";
    }

    // Xử lý các action cập nhật trạng thái
    @PostMapping("/{orderId}/{action}")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(@PathVariable("orderId") Long orderId,
                                               @PathVariable("action") String action) {
        try {
            Order order = orderService.findById(orderId);
            if (order == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Đơn hàng không tồn tại"));
            }

            switch (action) {
                case "confirm":
                    orderService.confirmOrder(order);
                    break;
                case "ship":
                    orderService.startShipping(order);
                    break;
                case "deliver":
                    orderService.markDelivered(order);
                    break;
                case "admin-confirm-payment":
                    orderService.adminConfirmPayment(order);
                    break;
                default:
                    return ResponseEntity.ok(Map.of("success", false, "message", "Hành động không hợp lệ"));
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("success", false, "message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}
