package roomescape.auth.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.InvalidRequestException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BCryptPasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("원본 비밀번호와 인코딩된 비밀번호가 일치하는지 검증한다.")
    void matches_returnsTrue_whenRawPasswordMatchesEncodedPassword() {
        String encodedPassword = passwordEncoder.encode("password");

        assertThat(passwordEncoder.matches("password", encodedPassword)).isTrue();
    }

    @Test
    @DisplayName("원본 비밀번호가 다르면 일치하지 않는다.")
    void matches_returnsFalse_whenRawPasswordDoesNotMatchEncodedPassword() {
        String encodedPassword = passwordEncoder.encode("password");

        assertThat(passwordEncoder.matches("wrong-password", encodedPassword)).isFalse();
    }

    @Test
    @DisplayName("같은 비밀번호라도 매번 다른 인코딩 결과를 만든다.")
    void encode_returnsDifferentValue_eachTime() {
        String first = passwordEncoder.encode("password");
        String second = passwordEncoder.encode("password");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("인코딩 형식이 다르면 일치하지 않는다.")
    void matches_returnsFalse_whenEncodedPasswordFormatIsInvalid() {
        assertThat(passwordEncoder.matches("password", "password")).isFalse();
    }

    @Test
    @DisplayName("이미 저장된 BCrypt 비밀번호와 원본 비밀번호가 일치하는지 검증한다.")
    void matches_returnsTrue_whenPasswordMatchesStoredBCryptPassword() {
        String storedPassword = "$2a$10$oQHYDAbw.CEJjCIwDX8nf.K7NErkkOgdqo00e0NKO.YCZJqS4XOjK";

        assertThat(passwordEncoder.matches("password", storedPassword)).isTrue();
    }

    @Test
    @DisplayName("비밀번호가 비어 있으면 인코딩할 수 없다.")
    void encode_throwsException_whenRawPasswordIsBlank() {
        assertThatThrownBy(() -> passwordEncoder.encode(" "))
                .isInstanceOf(InvalidRequestException.class);
    }
}
