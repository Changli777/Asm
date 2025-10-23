package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.User;
import poly.edu.service.SessionService;
import poly.edu.service.UserService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Controller
public class AccountController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;

    @GetMapping("/account")
    public String accountPage(Model model) {
        User currentUser = (User) sessionService.get("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        return "user/account";
    }

    // Hiển thị form chỉnh sửa
    @GetMapping("/account/edit")
    public String editAccount(Model model) {
        User currentUser = (User) sessionService.get("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        return "user/edit"; // edit.html nằm trong templates/user/
    }

    // Xử lý submit form chỉnh sửa
    @PostMapping("/account/edit")
    public String updateAccount(@ModelAttribute("user") User formUser,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) sessionService.get("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Cập nhật các field có thể thay đổi
        currentUser.setFullName(formUser.getFullName());
        currentUser.setUsername(formUser.getUsername());
        currentUser.setEmail(formUser.getEmail());
        currentUser.setGender(formUser.getGender());
        currentUser.setDateOfBirth(formUser.getDateOfBirth());
        currentUser.setPhone(formUser.getPhone());
        currentUser.setAddress(formUser.getAddress());
        currentUser.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Lưu vào DB
        userService.update(currentUser);

        // Cập nhật session
        sessionService.set("currentUser", currentUser);

        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        return "redirect:/account";
    }
}