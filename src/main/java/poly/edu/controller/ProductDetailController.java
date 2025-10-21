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

import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductDetailController {

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private CategoryDAO categoryDAO;

    @GetMapping("/{id}")
    public String showProductDetail(@PathVariable("id") Long id, Model model) {
        Product product = productDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        // tăng viewsCount
        product.setViewsCount(product.getViewsCount() == null ? 1 : product.getViewsCount() + 1);
        productDAO.save(product);

        // lấy danh mục hiện tại
        Category category = product.getCategory();

        // lấy các sản phẩm cùng danh mục (trừ chính nó)
        List<Product> relatedProducts = category.getProducts().stream()
                .filter(p -> !p.getProductId().equals(product.getProductId()))
                .limit(4) // giới hạn 4 sản phẩm liên quan
                .toList();

        model.addAttribute("product", product);
        model.addAttribute("category", category);
        model.addAttribute("relatedProducts", relatedProducts);

        return "productDetail";
    }

    // xử lý 404 (nếu muốn hiển thị trang riêng)
    @ExceptionHandler(ResponseStatusException.class)
    public String handleNotFound(ResponseStatusException ex, Model model) {
        model.addAttribute("message", ex.getReason());
        return "error/404";
    }
}