package poly.edu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/order/**", "/orders/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/account/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().permitAll()
                )

                // ---------------------------
                // 🔥 REMEMBER ME CHUẨN SPRING SECURITY
                // ---------------------------
                .rememberMe(remember -> remember
                        .key("SECRET_KEY_REMEMBER_ME_987654321") // key bắt buộc
                        .rememberMeParameter("remember-me")       // name của checkbox trong form
                        .tokenValiditySeconds(7 * 24 * 60 * 60)   // cookie tồn tại 7 ngày
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me") // xoá cookie remember-me
                        .permitAll()
                )

                .exceptionHandling(e -> e.accessDeniedPage("/error/403"))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
