package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import poly.edu.dao.ProductDAO;
import poly.edu.dao.CategoryDAO;
import poly.edu.entity.Product;
import poly.edu.entity.Category;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductDetailController {

    private static final int PAGE_SIZE = 4;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private CategoryDAO categoryDAO;

    @GetMapping("/{id}")
    public String showProductDetail(
            @PathVariable("id") Long id,
            @RequestParam(value = "relatedPage", required = false) Integer relatedPageParam,
            Model model) {

        // 1. Lấy sản phẩm và tăng viewsCount
        Product product = productDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        product.setViewsCount(product.getViewsCount() == null ? 1 : product.getViewsCount() + 1);
        productDAO.save(product);

        // 2. Lấy danh mục hiện tại
        Category category = product.getCategory();

        // 3. Lấy toàn bộ sản phẩm cùng danh mục (trừ chính sản phẩm)
        List<Product> relatedAll = category.getProducts().stream()
                .filter(p -> !p.getProductId().equals(product.getProductId()))
                .toList();

        // 4. Xử lý trang hiện tại cho related products
        int PAGE_SIZE = 4;
        int relatedPage = (relatedPageParam == null || relatedPageParam < 1) ? 1 : relatedPageParam;

        List<Product> relatedPageItems = circularPage(relatedAll, relatedPage, PAGE_SIZE);
        int totalRelatedPages = computeTotalPages(relatedAll.size(), PAGE_SIZE);
        relatedPage = normalizePage(relatedPage, totalRelatedPages);

        String relatedPrevUrl = buildUrl("relatedPage", prevOf(relatedPage, totalRelatedPages), id);
        String relatedNextUrl = buildUrl("relatedPage", nextOf(relatedPage, totalRelatedPages), id);

        // 5. Thêm vào model
        model.addAttribute("product", product);
        model.addAttribute("category", category);
        model.addAttribute("relatedProducts", relatedPageItems);
        model.addAttribute("relatedPrevUrl", relatedPrevUrl);
        model.addAttribute("relatedNextUrl", relatedNextUrl);
        model.addAttribute("relatedPageCurrent", relatedPage);
        model.addAttribute("relatedTotalPages", totalRelatedPages);

        return "productDetail";
    }




    // ---------- helper methods ----------
    private List<Product> circularPage(List<Product> all, int requestedPage, int pageSize) {
        List<Product> result = new ArrayList<>();
        if (all == null || all.isEmpty()) return result;

        int total = all.size();
        int totalPages = computeTotalPages(total, pageSize);
        int page = normalizePage(requestedPage, totalPages);

        int startIndex = (page - 1) * pageSize;
        for (int i = 0; i < pageSize; i++) {
            int idx = (startIndex + i) % total;
            result.add(all.get(idx));
        }
        return result;
    }

    private int computeTotalPages(int totalItems, int pageSize) {
        if (totalItems <= 0) return 1;
        return (totalItems + pageSize - 1) / pageSize;
    }

    private int normalizePage(int p, int totalPages) {
        if (totalPages <= 0) return 1;
        int mod = ((p - 1) % totalPages + totalPages) % totalPages;
        return mod + 1;
    }

    private int prevOf(int current, int totalPages) {
        if (totalPages <= 1) return 1;
        return current == 1 ? totalPages : (current - 1);
    }

    private int nextOf(int current, int totalPages) {
        if (totalPages <= 1) return 1;
        return current == totalPages ? 1 : (current + 1);
    }

    private String buildUrl(String pageParam, int page, Long productId) {
        return String.format("/product/%d?%s=%d", productId, pageParam, page);
    }

}