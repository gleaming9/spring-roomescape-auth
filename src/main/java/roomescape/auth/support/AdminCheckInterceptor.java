package roomescape.auth.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.exception.ForbiddenException;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor {
    private static final String ADMIN_REQUIRED_MESSAGE = "관리자 권한이 필요합니다.";

    private final LoginMemberInfoResolver loginMemberInfoResolver;

    public AdminCheckInterceptor(LoginMemberInfoResolver loginMemberInfoResolver) {
        this.loginMemberInfoResolver = loginMemberInfoResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        LoginMemberInfo loginMember = loginMemberInfoResolver.resolve(request);
        if (!loginMember.isAdmin()) {
            throw new ForbiddenException(ADMIN_REQUIRED_MESSAGE);
        }

        return true;
    }
}
