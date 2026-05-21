package roomescape.auth.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

import static org.assertj.core.api.Assertions.assertThat;

class LoginMemberInfoTest {

    @Test
    @DisplayName("회원의 역할과 관리 매장 정보를 포함한다.")
    void from_containsRoleAndStoreId() {
        Member member = new Member(1L, "manager@example.com", "encoded-password", "매니저", Role.MANAGER, 1L);

        LoginMemberInfo loginMemberInfo = LoginMemberInfo.from(member);

        assertThat(loginMemberInfo.id()).isEqualTo(1L);
        assertThat(loginMemberInfo.email()).isEqualTo("manager@example.com");
        assertThat(loginMemberInfo.name()).isEqualTo("매니저");
        assertThat(loginMemberInfo.role()).isEqualTo(Role.MANAGER);
        assertThat(loginMemberInfo.storeId()).isEqualTo(1L);
    }
}
