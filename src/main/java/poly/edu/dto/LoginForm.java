package poly.edu.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class LoginForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Chưa nhập tài khoản")
    private String username;

    @NotBlank(message = "Chưa nhập mật khẩu")
    private String password;

    // Ghi nhớ đăng nhập
    private boolean remember = false;

    public LoginForm() {
    }

    public LoginForm(String username, String password, boolean remember) {
        this.username = username;
        this.password = password;
        this.remember = remember;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRemember() {
        return remember;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
    }

    @Override
    public String toString() {
        return "LoginForm{" +
                "username='" + username + '\'' +
                ", remember=" + remember +
                '}';
    }
}
