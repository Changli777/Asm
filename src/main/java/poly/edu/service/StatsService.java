package poly.edu.service;

import poly.edu.dto.CategoryStatsDTO;

import java.util.List;

public interface StatsService {
    /**
     * Lấy thống kê doanh thu theo danh mục.
     * @return List các CategoryStatsDTO
     */
    List<CategoryStatsDTO> getCategoryRevenueStats();
}