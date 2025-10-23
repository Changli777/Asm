package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.dao.ProductDAO;
import poly.edu.dao.CategoryDAO;
import poly.edu.entity.Product;

import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private CategoryDAO categoryDAO;

    // Trong ProductController.java

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            // Thay thế minPrice, maxPrice bằng priceRange (String)
            @RequestParam(value = "priceRange", required = false) String priceRange,
            Model model
    ) {
        // 1. KHỞI TẠO VÀ XỬ LÝ KHOẢNG GIÁ
        Double minPrice = null;
        Double maxPrice = null;

        if (priceRange != null && !priceRange.isEmpty()) {
            switch (priceRange) {
                case "1": // Dưới 5 triệu
                    minPrice = 0.0;
                    maxPrice = 5000000.0;
                    break;
                case "2": // 5 - 10 triệu
                    minPrice = 5000000.0;
                    maxPrice = 10000000.0;
                    break;
                case "3": // 10 - 20 triệu
                    minPrice = 10000000.0;
                    maxPrice = 20000000.0;
                    break;
                case "4": // 20 - 30 triệu
                    minPrice = 20000000.0;
                    maxPrice = 30000000.0;
                    break;
                case "5": // Trên 30 triệu
                    minPrice = 30000000.0;
                    // maxPrice giữ nguyên là null (không giới hạn trên)
                    break;
                default:
                    break;
            }
        }

        // 2. CHUẨN BỊ KEYWORD (Đảm bảo keyword rỗng "" được chuyển thành null)
        String finalKeyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;

        // 3. GỌI DAO VỚI CÁC THAM SỐ ĐÃ CHUYỂN ĐỔI
        List<Product> results = productDAO.searchProducts(finalKeyword, categoryId, minPrice, maxPrice);

        // 4. THÊM THUỘC TÍNH VÀO MODEL
        model.addAttribute("results", results);
        model.addAttribute("categories", categoryDAO.findAll());

        // Thêm các tham số tìm kiếm vào Model để giữ lại trạng thái trên thanh tìm kiếm
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("priceRange", priceRange); // Giữ lại priceRange để Thymeleaf chọn đúng option

        return "search";
    }
}
