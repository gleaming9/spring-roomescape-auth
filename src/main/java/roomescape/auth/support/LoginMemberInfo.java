package roomescape.auth.support;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

import java.util.Objects;

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

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isManager() {
        return role == Role.MANAGER;
    }

    public boolean canManageStore(Long storeId) {
        if (isAdmin()) {
            return true;
        }

        return isManager() && Objects.equals(this.storeId, storeId);
    }
}
