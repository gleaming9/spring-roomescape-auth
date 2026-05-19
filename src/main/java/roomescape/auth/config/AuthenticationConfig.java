package roomescape.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.support.LoginCheckInterceptor;

@Configuration
public class AuthenticationConfig implements WebMvcConfigurer {
    private final LoginCheckInterceptor loginCheckInterceptor;

    public AuthenticationConfig(LoginCheckInterceptor loginCheckInterceptor) {
        this.loginCheckInterceptor = loginCheckInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns(
                        "/members/me",
                        "/reservations/**"
                );
    }
}
