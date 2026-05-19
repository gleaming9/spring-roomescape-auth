package roomescape.member.dto;

import roomescape.auth.support.LoginMemberInfo;
import roomescape.member.domain.Member;

public record MemberResponse(
        Long id,
        String email,
        String name
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getName());
    }

    public static MemberResponse from(LoginMemberInfo loginMember) {
        return new MemberResponse(loginMember.id(), loginMember.email(), loginMember.name());
    }
}
