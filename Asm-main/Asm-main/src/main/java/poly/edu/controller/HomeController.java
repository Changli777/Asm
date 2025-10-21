package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import poly.edu.dao.CategoryDAO;
import poly.edu.dao.ProductDAO;
import poly.edu.entity.Category;
import poly.edu.entity.Product;

import java.util.List;

@Controller
public class HomeController {

    //Link: http://localhost:8080/home

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private ProductDAO productDAO;
    @GetMapping("/home")
    public String home(Model model) {
        try {
            List<Category> categories = categoryDAO.findAll();
            model.addAttribute("categories", categories);

            // Thử giảm số lượng xuống để test
            List<Product> newProducts = productDAO.findNewProducts(4); // giảm từ 8 xuống 4
            List<Product> saleProducts = productDAO.findSaleProducts(4);
            List<Product> hotProducts = productDAO.findFeaturedProducts(4);

            // Debug: In ra console xem có data không
            System.out.println("Categories: " + categories.size());
            System.out.println("New Products: " + newProducts.size());

            // Set categoryName
            for (Product product : newProducts) {
                setCategoryName(product);
            }
            for (Product product : saleProducts) {
                setCategoryName(product);
            }
            for (Product product : hotProducts) {
                setCategoryName(product);
            }

            model.addAttribute("newProducts", newProducts);
            model.addAttribute("saleProducts", saleProducts);
            model.addAttribute("hotProducts", hotProducts);

            return "home";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private void setCategoryName(Product product) {
        if (product.getCategoryId() != null) {
            categoryDAO.findById(product.getCategoryId()).ifPresent(category -> {
                product.setCategoryName(category.getCategoryName());
            });
        }
    }
}