package poly.edu.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.CategoryDAO;
import poly.edu.entity.Category;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryDAO categoryDAO;

    // ✅ Hiển thị danh sách loại hàng
    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryDAO.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("category", new Category()); // để dùng cho form thêm mới
        return "admin/categories"; // trỏ đến file templates/admin/categories.html
    }

    // ✅ Thêm hoặc cập nhật loại hàng
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category) {
        categoryDAO.save(category);
        return "redirect:/admin/categories";
    }

    // ✅ Sửa (load dữ liệu lên form)
    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable("id") Long id, Model model) {
        Optional<Category> categoryOpt = categoryDAO.findById(id);
        if (categoryOpt.isPresent()) {
            model.addAttribute("category", categoryOpt.get());
        } else {
            model.addAttribute("category", new Category());
        }
        model.addAttribute("categories", categoryDAO.findAll());
        return "admin/categories";
    }

    // ✅ Xóa
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        categoryDAO.deleteById(id);
        return "redirect:/admin/categories";
    }
}
