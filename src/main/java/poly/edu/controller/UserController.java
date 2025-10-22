package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.UserDAO;
import poly.edu.entity.User;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserDAO userDAO;

    // Hiển thị danh sách người dùng
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userDAO.findAll();
        model.addAttribute("users", users);
        model.addAttribute("user", new User());
        return "user/list"; // templates/user/list.html
    }

    // Lưu người dùng (thêm hoặc cập nhật)
    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") User user) {
        userDAO.save(user);
        return "redirect:/admin/users";
    }

    // Hiển thị form sửa
    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable("id") Long id, Model model) {
        User existingUser = userDAO.findById(id).orElse(null);
        List<User> users = userDAO.findAll();
        model.addAttribute("users", users);
        model.addAttribute("user", existingUser);
        return "user/list";
    }

    // Xóa người dùng
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userDAO.deleteById(id);
        return "redirect:/admin/users";
    }
}
