package roomescape.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.InvalidRequestException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    @DisplayName("관리자는 관리 매장 없이 생성할 수 있다.")
    void createAdmin_success_withoutStoreId() {
        Member member = new Member("admin@example.com", "encoded-password", "어드민", Role.ADMIN, null);

        assertThat(member.getRole()).isEqualTo(Role.ADMIN);
        assertThat(member.getStoreId()).isNull();
    }

    @Test
    @DisplayName("매니저는 관리 매장 없이 생성할 수 없다.")
    void createManager_fail_withoutStoreId() {
        assertThatThrownBy(() -> new Member("manager@example.com", "encoded-password", "매니저", Role.MANAGER, null))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("관리자가 아닌 회원은 관리 매장을 가질 수 없다.")
    void createNonManager_fail_withStoreId() {
        assertThatThrownBy(() -> new Member("admin@example.com", "encoded-password", "어드민", Role.ADMIN, 1L))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> new Member("user@example.com", "encoded-password", "회원", Role.USER, 1L))
                .isInstanceOf(InvalidRequestException.class);
    }
}
