package poly.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatsDTO {
    private String categoryName;
    private Long totalSoldCount;
    private BigDecimal totalRevenue;
}