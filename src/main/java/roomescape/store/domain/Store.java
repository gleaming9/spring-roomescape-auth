package roomescape.store.domain;

import lombok.Getter;
import roomescape.global.exception.InvalidRequestException;

import java.util.Objects;

@Getter
public class Store {
    private final Long id;
    private final String name;

    public Store(String name) {
        this(null, name);
    }

    public Store(Long id, String name) {
        validate(name);
        this.id = id;
        this.name = name;
    }

    private void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("매장 이름은 비어 있을 수 없습니다.");
        }
    }

    public Store withId(Long id) {
        if (id == null) {
            throw new InvalidRequestException("매장 id는 비어 있을 수 없습니다.");
        }
        if (this.id != null) {
            throw new InvalidRequestException("이미 식별자가 존재하는 매장입니다.");
        }
        return new Store(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Store store)) return false;
        return id != null && Objects.equals(id, store.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
