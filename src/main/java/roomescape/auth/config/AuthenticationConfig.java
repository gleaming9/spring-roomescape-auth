package roomescape.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.support.AdminCheckInterceptor;
import roomescape.auth.support.LoginCheckInterceptor;
import roomescape.auth.support.LoginMemberArgumentResolver;

import java.util.List;

@Configuration
public class AuthenticationConfig implements WebMvcConfigurer {
    private final LoginCheckInterceptor loginCheckInterceptor;
    private final AdminCheckInterceptor adminCheckInterceptor;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    public AuthenticationConfig(LoginCheckInterceptor loginCheckInterceptor,
                                AdminCheckInterceptor adminCheckInterceptor,
                                LoginMemberArgumentResolver loginMemberArgumentResolver) {
        this.loginCheckInterceptor = loginCheckInterceptor;
        this.adminCheckInterceptor = adminCheckInterceptor;
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns(
                        "/members/me",
                        "/reservations/**",
                        "/admin/reservations/**",
                        "/admin/themes/**",
                        "/admin/times/**"
                );

        registry.addInterceptor(adminCheckInterceptor)
                .addPathPatterns(
                        "/admin/themes/**",
                        "/admin/times/**"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }
}
