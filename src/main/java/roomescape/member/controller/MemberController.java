package roomescape.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.support.LoginMember;
import roomescape.auth.support.LoginMemberInfo;
import roomescape.member.dto.MemberResponse;

@RestController
@RequestMapping("/members")
public class MemberController {
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(@LoginMember LoginMemberInfo loginMember) {
        return ResponseEntity.ok(MemberResponse.from(loginMember));
    }
}
