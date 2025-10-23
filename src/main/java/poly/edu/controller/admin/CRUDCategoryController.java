package poly.edu.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.Category;
import poly.edu.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/categories")
public class CRUDCategoryController {

    @Autowired
    private CategoryService categoryService; // dùng service

    // Hiển thị danh sách + tìm kiếm
    @GetMapping
    public String listCategories(Model model,
                                 @RequestParam(value = "q", required = false) String q) {
        List<Category> categories = categoryService.findAll();

        if (StringUtils.hasText(q)) {
            String keyword = q.trim().toLowerCase();
            categories = categories.stream()
                    .filter(c -> c.getCategoryName() != null &&
                            c.getCategoryName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }

        model.addAttribute("categories", categories);
        model.addAttribute("categoryForm", new Category());
        model.addAttribute("searchQuery", q);
        return "admin/category"; // View file: templates/admin/category.html
    }

    // Form tạo mới hoặc sửa
    @GetMapping("/create")
    public String createOrEditForm(@RequestParam(value = "id", required = false) Long id,
                                   Model model) {
        Category category = (id != null) ? categoryService.findById(id) : new Category();
        model.addAttribute("categoryForm", category != null ? category : new Category());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/category";
    }

    // Lưu (Tạo mới hoặc cập nhật)
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("categoryForm") Category form,
                               RedirectAttributes redirectAttrs,
                               Model model) {
        if (!StringUtils.hasText(form.getCategoryName())) {
            model.addAttribute("error", "Chưa nhập tên loại hàng");
            model.addAttribute("categories", categoryService.findAll());
            return "admin/category";
        }

        boolean isNew = (form.getCategoryId() == null || form.getCategoryId() == 0);

        // Kiểm tra trùng tên (case-insensitive)
        boolean nameExists = categoryService.findAll().stream()
                .anyMatch(c -> c.getCategoryName().equalsIgnoreCase(form.getCategoryName())
                        && !c.getCategoryId().equals(form.getCategoryId()));

        if (nameExists) {
            model.addAttribute("error", "Tên loại hàng đã tồn tại");
            model.addAttribute("categories", categoryService.findAll());
            return "admin/category";
        }

        try {
            if (isNew) {
                categoryService.create(form);
                redirectAttrs.addFlashAttribute("msg", "Tạo loại hàng mới thành công.");
            } else {
                categoryService.update(form);
                redirectAttrs.addFlashAttribute("msg", "Cập nhật loại hàng thành công.");
            }
            return "redirect:/admin/categories";
        } catch (Exception ex) {
            model.addAttribute("error", "Lỗi khi lưu loại hàng: " + ex.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/category";
        }
    }

    // Xóa loại hàng
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id,
                                 RedirectAttributes redirectAttrs,
                                 Model model) {
        try {
            categoryService.deleteById(id);
            redirectAttrs.addFlashAttribute("msg", "Xóa loại hàng thành công.");
            return "redirect:/admin/categories";
        } catch (Exception ex) {
            model.addAttribute("error", "Lỗi khi xóa: " + ex.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("categoryForm", new Category());
            return "admin/category";
        }
    }
}