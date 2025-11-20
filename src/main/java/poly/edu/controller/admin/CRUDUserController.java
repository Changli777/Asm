package poly.edu.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.Role;
import poly.edu.entity.User;
import poly.edu.entity.UserRole;
import poly.edu.service.UserService;
import poly.edu.dao.RoleDAO;
import poly.edu.dao.UserRoleDAO;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/user")
public class CRUDUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private UserRoleDAO userRoleDAO;

    /* ===========================
     * HIỂN THỊ DANH SÁCH USER
     * =========================== */
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "q", required = false) String q) {

        List<User> users = userService.findAll();

        if (StringUtils.hasText(q)) {
            String keyword = q.toLowerCase();
            users = users.stream()
                    .filter(u ->
                            (u.getUsername() != null && u.getUsername().toLowerCase().contains(keyword)) ||
                                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword)) ||
                                    (u.getPhone() != null && u.getPhone().toLowerCase().contains(keyword))
                    )
                    .collect(Collectors.toList());
        }

        model.addAttribute("users", users);
        model.addAttribute("roles", roleDAO.findAll());
        model.addAttribute("userForm", new User());
        model.addAttribute("searchQuery", q);

        return "admin/user";
    }

    /* ===========================
     * MỞ TRANG CREATE / EDIT USER
     * =========================== */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {

        User user = userService.findById(id);
        if (user == null) {
            model.addAttribute("error", "Không tìm thấy user!");
            return "redirect:/admin/user";
        }

        model.addAttribute("userForm", user);
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", roleDAO.findAll());

        // role hiện tại (nếu có)
        String currentRole = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .findFirst()
                .orElse(null);

        model.addAttribute("currentRole", currentRole);

        return "admin/user";
    }

    /* ===========================
     * LƯU USER (CREATE + UPDATE)
     * =========================== */
    @PostMapping("/save")
    public String save(@ModelAttribute("userForm") User form,
                       @RequestParam(value = "roleId") String roleId,
                       @RequestParam(value = "rawPassword", required = false) String rawPassword,
                       RedirectAttributes redirectAttrs,
                       Model model) {

        // ===== VALIDATION CƠ BẢN =====
        if (!StringUtils.hasText(form.getUsername())) {
            model.addAttribute("error", "Chưa nhập username");
            return reload(model);
        }
        if (!StringUtils.hasText(form.getEmail())) {
            model.addAttribute("error", "Chưa nhập email");
            return reload(model);
        }
        if (!StringUtils.hasText(form.getFullName())) {
            model.addAttribute("error", "Chưa nhập họ tên");
            return reload(model);
        }

        try {
            boolean isNew = (form.getUserId() == null);

            if (isNew) {
                // ========== CREATE ==========
                if (!StringUtils.hasText(rawPassword)) {
                    model.addAttribute("error", "Chưa nhập password cho user mới");
                    return reload(model);
                }

                if (userService.existsByUsername(form.getUsername())) {
                    model.addAttribute("error", "Username đã tồn tại");
                    return reload(model);
                }
                if (userService.existsByEmail(form.getEmail())) {
                    model.addAttribute("error", "Email đã tồn tại");
                    return reload(model);
                }

                // dùng password raw
                form.setPassword(rawPassword);

                // Lưu user trước
                User saved = userService.create(form);

                // Gán role
                assignRole(saved, roleId);

                redirectAttrs.addFlashAttribute("msg", "Tạo user mới thành công!");

            } else {
                // ========== UPDATE ==========
                User exist = userService.findById(form.getUserId());

                if (!exist.getUsername().equals(form.getUsername()) &&
                        userService.existsByUsername(form.getUsername())) {
                    model.addAttribute("error", "Username đã tồn tại");
                    return reload(model);
                }

                if (!exist.getEmail().equals(form.getEmail()) &&
                        userService.existsByEmail(form.getEmail())) {
                    model.addAttribute("error", "Email đã tồn tại");
                    return reload(model);
                }

                // cập nhật fields
                exist.setUsername(form.getUsername());
                exist.setEmail(form.getEmail());
                exist.setFullName(form.getFullName());
                exist.setGender(form.getGender());
                exist.setDateOfBirth(form.getDateOfBirth());
                exist.setPhone(form.getPhone());
                exist.setAddress(form.getAddress());
                exist.setProvider(form.getProvider());
                exist.setProviderId(form.getProviderId());

                // cập nhật password (nếu nhập)
                if (StringUtils.hasText(rawPassword)) {
                    exist.setPassword(rawPassword);
                }

                userService.update(exist);

                // cập nhật role
                assignRole(exist, roleId);

                redirectAttrs.addFlashAttribute("msg", "Cập nhật user thành công!");
            }

            return "redirect:/admin/user";

        } catch (Exception ex) {
            model.addAttribute("error", "Lỗi khi lưu user: " + ex.getMessage());
            return reload(model);
        }
    }

    /* ===========================
     * XÓA USER
     * =========================== */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id,
                         RedirectAttributes redirectAttrs,
                         Model model) {

        try {
            userService.deleteById(id);
            redirectAttrs.addFlashAttribute("msg", "Xóa user thành công!");
            return "redirect:/admin/user";

        } catch (Exception ex) {
            model.addAttribute("error", "Lỗi khi xóa: " + ex.getMessage());
            return reload(model);
        }
    }

    /* ===========================
     * HÀM PHỤ: GÁN ROLE
     * =========================== */
    private void assignRole(User user, String roleId) {

        // Xóa role cũ
        userRoleDAO.deleteAllByUser(user);

        Role role = roleDAO.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + roleId));

        UserRole ur = UserRole.builder()
                .user(user)
                .role(role)
                .build();

        userRoleDAO.save(ur);
    }

    /* ===========================
     * HÀM PHỤ: LOAD LẠI DỮ LIỆU SAU LỖI
     * =========================== */
    private String reload(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", roleDAO.findAll());
        return "admin/user";
    }
}
