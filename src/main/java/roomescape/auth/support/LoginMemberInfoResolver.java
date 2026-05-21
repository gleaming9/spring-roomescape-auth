package roomescape.auth.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import roomescape.global.exception.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Component
public class LoginMemberInfoResolver {
    private static final String AUTHENTICATION_REQUIRED_MESSAGE = "로그인이 필요합니다.";

    private final MemberRepository memberRepository;

    public LoginMemberInfoResolver(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public LoginMemberInfo resolve(HttpServletRequest request) {
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
