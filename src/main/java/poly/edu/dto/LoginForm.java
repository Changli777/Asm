package poly.edu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginForm {
    @NotBlank(message = "Chưa nhập tài khoản username/email/sdt")
    private String username;

    @NotBlank(message = "Chưa nhập mật khẩu")
    private String password;

    // checkbox "remember"
    private boolean remember;
}
