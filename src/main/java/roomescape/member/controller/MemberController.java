package roomescape.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.auth.support.LoginMember;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;

public class MemberController {
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(@LoginMember Member member) {
        return ResponseEntity.ok(MemberResponse.from(member));
    }
}
