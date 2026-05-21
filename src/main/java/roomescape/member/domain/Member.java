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
    private final Role role;
    private final Long storeId;

    public Member(String email, String password, String name) {
        this(null, email, password, name);
    }

    public Member(Long id, String email, String password, String name) {
        this(id, email, password, name, Role.USER, null);
    }

    public Member(String email, String password, String name, Role role, Long storeId) {
        this(null, email, password, name, role, storeId);
    }

    public Member(Long id, String email, String password, String name, Role role, Long storeId) {
        validate(email, password, name, role, storeId);

        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.storeId = storeId;
    }

    private void validate(String email, String password, String name, Role role, Long storeId) {
        if (email == null || email.isBlank()) {
            throw new InvalidRequestException("회원 이메일은 비어 있을 수 없습니다.");
        }
        if (password == null || password.isBlank()) {
            throw new InvalidRequestException("회원 비밀번호는 비어 있을 수 없습니다.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("회원 이름은 비어 있을 수 없습니다.");
        }
        if (role == null) {
            throw new InvalidRequestException("회원 역할은 비어 있을 수 없습니다.");
        }
        if (role == Role.MANAGER && storeId == null) {
            throw new InvalidRequestException("매니저의 매장은 비어 있을 수 없습니다.");
        }
        if (role == Role.USER && storeId != null) {
            throw new InvalidRequestException("일반 회원은 관리 매장을 가질 수 없습니다.");
        }
    }

    public Member withId(Long id) {
        if (id == null) {
            throw new InvalidRequestException("회원 id는 비어 있을 수 없습니다.");
        }
        if (this.id != null) {
            throw new InvalidRequestException("이미 식별자가 존재하는 회원입니다.");
        }
        return new Member(id, email, password, name, role, storeId);
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
