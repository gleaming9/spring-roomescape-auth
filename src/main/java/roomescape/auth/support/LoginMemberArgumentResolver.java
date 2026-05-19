package roomescape.auth.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.global.exception.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String AUTHENTICATION_REQUIRED_MESSAGE = "로그인이 필요합니다.";
    private final MemberRepository memberRepository;

    public LoginMemberArgumentResolver(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && LoginMemberInfo.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new UnauthorizedException(AUTHENTICATION_REQUIRED_MESSAGE);
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new UnauthorizedException(AUTHENTICATION_REQUIRED_MESSAGE);
        }

        Object memberId = session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        if (!(memberId instanceof Long id)) {
            throw new UnauthorizedException(AUTHENTICATION_REQUIRED_MESSAGE);
        }

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException(AUTHENTICATION_REQUIRED_MESSAGE));

        return LoginMemberInfo.from(member);
    }
}
