package poly.edu.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.User;
import poly.edu.service.UserService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/user")
public class CRUDUserController {

    @Autowired
    private UserService userService;

    // Formatter cho dd/MM/yyyy (theo yêu cầu)
    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Hiển thị trang chính /admin/user
     * - model attributes: users (list), userForm (empty)
     * - param q: tìm theo username hoặc email hoặc phone (contains)
     */
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "q", required = false) String q) {
        List<User> users = userService.findAll();
        if (StringUtils.hasText(q)) {
            String qLower = q.trim().toLowerCase();
            users = users.stream()
                    .filter(u -> (u.getUsername() != null && u.getUsername().toLowerCase().contains(qLower))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(qLower))
                            || (u.getPhone() != null && u.getPhone().toLowerCase().contains(qLower)))
                    .collect(Collectors.toList());
        }
        model.addAttribute("users", users);
        model.addAttribute("userForm", new User());
        model.addAttribute("searchQuery", q);
        return "admin/user";
    }

    /**
     * Mở form tạo (blank) hoặc edit nếu có id param
     * GET /admin/user/create?id=...
     */
    @GetMapping("/create")
    public String createForm(@RequestParam(value = "id", required = false) Long id, Model model) {
        if (id != null) {
            try {
                User u = userService.findById(id);
                model.addAttribute("userForm", u);
            } catch (Exception ex) {
                // không tìm thấy -> hiển thị form rỗng kèm thông báo
                model.addAttribute("userForm", new User());
                model.addAttribute("error", "Không tìm thấy user với id = " + id);
            }
        } else {
            model.addAttribute("userForm", new User());
        }
        model.addAttribute("users", userService.findAll());
        return "admin/user";
    }

    /**
     * Lưu (create hoặc update) - dùng POST
     * POST /admin/user/save/{id}
     * - Nếu id > 0 => update; nếu id == 0 hoặc null => create
     * - dateOfBirth được gửi dưới dạng text dd/MM/yyyy (param name: dateOfBirthStr)
     * - rawPassword: nếu rỗng khi update => giữ mật khẩu cũ
     */
    @PostMapping("/save/{id}")
    public String save(@PathVariable("id") Long id,
                       @ModelAttribute("userForm") User form,
                       @RequestParam(value = "rawPassword", required = false) String rawPassword,
                       Model model,
                       RedirectAttributes redirectAttrs) {

        // BASIC validation (as before)
        if (!StringUtils.hasText(form.getUsername())) {
            model.addAttribute("error", "Chưa nhập username");
            model.addAttribute("users", userService.findAll());
            model.addAttribute("userForm", form);
            return "admin/user";
        }
        if (!StringUtils.hasText(form.getEmail())) {
            model.addAttribute("error", "Chưa nhập email");
            model.addAttribute("users", userService.findAll());
            model.addAttribute("userForm", form);
            return "admin/user";
        }
        if (!StringUtils.hasText(form.getFullName())) {
            model.addAttribute("error", "Chưa nhập họ tên");
            model.addAttribute("users", userService.findAll());
            model.addAttribute("userForm", form);
            return "admin/user";
        }
        if (!StringUtils.hasText(form.getPhone())) {
            model.addAttribute("error", "Chưa nhập số điện thoại");
            model.addAttribute("users", userService.findAll());
            model.addAttribute("userForm", form);
            return "admin/user";
        }

        try {
            if (id != null && id > 0) {
                // UPDATE
                User exist = userService.findById(id);
                if (!exist.getUsername().equals(form.getUsername()) && userService.existsByUsername(form.getUsername())) {
                    model.addAttribute("error", "Username đã tồn tại");
                    model.addAttribute("users", userService.findAll());
                    model.addAttribute("userForm", form);
                    return "admin/user";
                }
                if (!exist.getEmail().equals(form.getEmail()) && userService.existsByEmail(form.getEmail())) {
                    model.addAttribute("error", "Email đã tồn tại");
                    model.addAttribute("users", userService.findAll());
                    model.addAttribute("userForm", form);
                    return "admin/user";
                }

                // cập nhật fields
                exist.setUsername(form.getUsername());
                exist.setEmail(form.getEmail());
                exist.setFullName(form.getFullName());
                exist.setGender(form.getGender());
                exist.setDateOfBirth(form.getDateOfBirth()); // trực tiếp binding từ input[type=date]
                exist.setPhone(form.getPhone());
                exist.setAddress(form.getAddress());
                exist.setRole(form.getRole());
                exist.setProvider(form.getProvider());
                exist.setProviderId(form.getProviderId());
                if (StringUtils.hasText(rawPassword)) {
                    exist.setPassword(rawPassword);
                }
                userService.update(exist);

                // flash message cho redirect
                redirectAttrs.addFlashAttribute("msg", "Cập nhật user thành công.");
            } else {
                // CREATE
                if (!StringUtils.hasText(rawPassword) && !StringUtils.hasText(form.getPassword())) {
                    model.addAttribute("error", "Chưa nhập password cho user mới");
                    model.addAttribute("users", userService.findAll());
                    model.addAttribute("userForm", form);
                    return "admin/user";
                }
                if (!StringUtils.hasText(form.getPassword()) && StringUtils.hasText(rawPassword)) {
                    form.setPassword(rawPassword);
                }
                if (userService.existsByUsername(form.getUsername())) {
                    model.addAttribute("error", "Username đã tồn tại");
                    model.addAttribute("users", userService.findAll());
                    model.addAttribute("userForm", form);
                    return "admin/user";
                }
                if (userService.existsByEmail(form.getEmail())) {
                    model.addAttribute("error", "Email đã tồn tại");
                    model.addAttribute("users", userService.findAll());
                    model.addAttribute("userForm", form);
                    return "admin/user";
                }
                userService.create(form);

                // flash message cho redirect
                redirectAttrs.addFlashAttribute("msg", "Tạo user mới thành công.");
            }
            return "redirect:/admin/user";
        } catch (Exception ex) {
            model.addAttribute("error", "Lỗi khi lưu user: " + ex.getMessage());
            model.addAttribute("users", userService.findAll());
            model.addAttribute("userForm", form);
            return "admin/user";
        }
    }


    /**
     * Xoá user bằng POST
     * POST /admin/user/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttrs) {
        try {
            userService.deleteById(id);
            redirectAttrs.addFlashAttribute("msg", "Xóa user thành công.");
            return "redirect:/admin/user";
        } catch (Exception ex) {
            model.addAttribute("error", "Lỗi khi xóa: " + ex.getMessage());
            model.addAttribute("users", userService.findAll());
            model.addAttribute("userForm", new User());
            return "admin/user";
        }
    }
}
