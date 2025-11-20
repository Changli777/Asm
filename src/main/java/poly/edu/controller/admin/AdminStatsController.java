package poly.edu.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import poly.edu.dto.CategoryStatsDTO;
import poly.edu.service.StatsService;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminStatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/statistics")
    public String showStatistics(Model model) {

        // Chỉ cần xử lý logic, không cần kiểm tra login hoặc role nữa
        List<CategoryStatsDTO> categoryStats = statsService.getCategoryRevenueStats();

        BigDecimal overallRevenue = categoryStats.stream()
                .map(CategoryStatsDTO::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("overallRevenue", overallRevenue);

        return "admin/statistics";
    }
}
