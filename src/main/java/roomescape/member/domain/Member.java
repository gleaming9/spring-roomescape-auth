package roomescape.member.domain;

import lombok.Getter;
import roomescape.global.exception.InvalidRequestException;

import java.util.Objects;

@Getter
public class Member {
    private final Long id;
    private final String email;
    private final String password;
    private final String name;

    public Member(String email, String password, String name) {
        this(null, email, password, name);
    }

    public Member(Long id, String email, String password, String name) {
        validate(email, password, name);

        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    private void validate(String email, String password, String name) {
        if (email == null || email.isBlank()) {
            throw new InvalidRequestException("회원 이메일은 비어 있을 수 없습니다.");
        }
        if (password == null || password.isBlank()) {
            throw new InvalidRequestException("회원 비밀번호는 비어 있을 수 없습니다.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("회원 이름은 비어 있을 수 없습니다.");
        }
    }

    public Member withId(Long id) {
        if (id == null) {
            throw new InvalidRequestException("회원 id는 비어 있을 수 없습니다.");
        }
        if (this.id != null) {
            throw new InvalidRequestException("이미 식별자가 존재하는 회원입니다.");
        }
        return new Member(id, email, password, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member member)) return false;
        return id != null && Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
