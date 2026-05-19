package roomescape.member.repository;

import roomescape.member.domain.Member;

import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);

    Member save(Member member);
}
