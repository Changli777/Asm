package poly.edu.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import poly.edu.dto.CategoryStatsDTO;
import poly.edu.service.StatsService;
import poly.edu.service.SessionService;
import poly.edu.entity.User;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminStatsController {

    @Autowired
    private StatsService statsService;

    @Autowired
    private SessionService sessionService;

    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        // ✅ Bắt buộc: Kiểm tra role ADMIN (ví dụ đơn giản, bạn nên dùng Spring Security)
        User currentUser = sessionService.get("currentUser");
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            // Có thể redirect về trang login hoặc trang 403
            return "redirect:/login";
        }

        // 1. Lấy dữ liệu thống kê
        List<CategoryStatsDTO> categoryStats = statsService.getCategoryRevenueStats();

        // 2. Tính tổng doanh thu toàn bộ (tùy chọn)
        BigDecimal overallRevenue = categoryStats.stream()
                .map(CategoryStatsDTO::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Thêm vào Model
        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("overallRevenue", overallRevenue);

        return "admin/statistics"; // Sẽ tạo file này ở bước tiếp theo
    }
}