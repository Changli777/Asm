package poly.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import poly.edu.interceptor.RememberMeInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RememberMeInterceptor rememberMeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rememberMeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/logout", "/register", "/css/**", "/js/**", "/images/**");
    }
}
