package poly.edu.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.OrderDAO;
import poly.edu.entity.Order;

import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderDAO orderDAO;

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", orderDAO.findAll());
        model.addAttribute("order", new Order());
        return "admin/orders"; // templates/admin/orders.html
    }

    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Optional<Order> opt = orderDAO.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("order", opt.get());
        } else {
            model.addAttribute("order", new Order());
        }
        model.addAttribute("orders", orderDAO.findAll());
        return "admin/orders";
    }

    @PostMapping("/updateStatus/{id}")
    public String updateStatus(@PathVariable Long id, @RequestParam("status") String status) {
        Optional<Order> opt = orderDAO.findById(id);
        if (opt.isPresent()) {
            Order order = opt.get();
            order.setStatus(status);
            orderDAO.save(order);
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderDAO.deleteById(id);
        return "redirect:/admin/orders";
    }
}
