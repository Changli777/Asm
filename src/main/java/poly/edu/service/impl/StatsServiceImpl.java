package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.ProductStatsDAO;
import poly.edu.dto.CategoryStatsDTO;
import poly.edu.service.StatsService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private ProductStatsDAO productStatsDAO;

    @Override
    public List<CategoryStatsDTO> getCategoryRevenueStats() {
        List<Object[]> results = productStatsDAO.getCategorySalesStats();

        // Chuyển đổi List<Object[]> sang List<CategoryStatsDTO>
        return results.stream().map(obj -> {
            String categoryName = (String) obj[0];

            // 1. Chuyển đổi Tổng số lượng đã bán (obj[1])
            Long totalSoldCount = 0L;
            if (obj[1] != null) {
                // Dùng Number và toLong cho an toàn, hoặc toString() rồi parse
                totalSoldCount = Long.valueOf(obj[1].toString());
            }

            // 2. Chuyển đổi Tổng doanh thu (obj[2])
            BigDecimal totalRevenue = BigDecimal.ZERO;
            if (obj[2] != null) {
                // ✅ KHẮC PHỤC LỖI TẠI ĐÂY: Tạo BigDecimal từ chuỗi, tránh lỗi ép kiểu trực tiếp
                totalRevenue = new BigDecimal(obj[2].toString());
            }

            return new CategoryStatsDTO(categoryName, totalSoldCount, totalRevenue);
        }).collect(Collectors.toList());
    }
}