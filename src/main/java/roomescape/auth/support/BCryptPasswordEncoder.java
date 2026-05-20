package roomescape.auth.support;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;
import roomescape.global.exception.InvalidRequestException;

@Component
public class BCryptPasswordEncoder implements PasswordEncoder {
    private static final int LOG_ROUNDS = 12;

    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidRequestException("비밀번호는 비어 있을 수 없습니다.");
        }

        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
