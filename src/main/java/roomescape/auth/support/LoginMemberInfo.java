package roomescape.auth.support;

import roomescape.member.domain.Member;

public record LoginMemberInfo(
        Long id,
        String email,
        String name
) {
    public static LoginMemberInfo from(Member member) {
        return new LoginMemberInfo(member.getId(), member.getEmail(), member.getName());
    }
}
