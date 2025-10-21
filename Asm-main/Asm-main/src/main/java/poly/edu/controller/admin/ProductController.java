package poly.edu.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.ProductDAO;
import poly.edu.entity.Product;

import java.util.Optional;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductDAO productDAO;

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productDAO.findAll());
        model.addAttribute("product", new Product());
        return "admin/products"; // trỏ tới templates/admin/products.html
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product) {
        productDAO.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Optional<Product> opt = productDAO.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("product", opt.get());
        } else {
            model.addAttribute("product", new Product());
        }
        model.addAttribute("products", productDAO.findAll());
        return "admin/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productDAO.deleteById(id);
        return "redirect:/admin/products";
    }
}
