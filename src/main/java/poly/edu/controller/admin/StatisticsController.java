package poly.edu.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import poly.edu.dao.OrderDAO;
import poly.edu.dao.ProductDAO;
import poly.edu.dao.UserDAO;

@Controller
public class StatisticsController {

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/admin/statistics")
    public String showStatistics(Model model) {
        long totalOrders = orderDAO.count();
        long totalProducts = productDAO.count();
        long totalUsers = userDAO.count();

        // Ví dụ: tổng doanh thu (nếu có cột total trong bảng orders)
        Double totalRevenue = orderDAO.getTotalRevenue();

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0);

        return "admin/statistics";
    }
}
