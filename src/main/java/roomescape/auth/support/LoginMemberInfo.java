package roomescape.auth.support;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public record LoginMemberInfo(
        Long id,
        String email,
        String name,
        Role role,
        Long storeId
) {
    public static LoginMemberInfo from(Member member) {
        return new LoginMemberInfo(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getStoreId()
        );
    }
}
