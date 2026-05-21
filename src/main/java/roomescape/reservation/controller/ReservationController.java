package roomescape.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.support.LoginMember;
import roomescape.auth.support.LoginMemberInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.dto.ReservationsResponse;
import roomescape.reservation.service.ReservationService;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @LoginMember LoginMemberInfo loginMember,
            @Valid @RequestBody ReservationCreateRequest reservationCreateRequest) {
        Reservation reservation = reservationService.create(
                loginMember.id(),
                reservationCreateRequest.storeId(),
                reservationCreateRequest.date(),
                reservationCreateRequest.timeId(),
                reservationCreateRequest.themeId()
        );

        return ResponseEntity.status(CREATED)
                .body(ReservationResponse.from(reservation));
    }

    @GetMapping
    public ResponseEntity<ReservationsResponse> list(@LoginMember LoginMemberInfo loginMember) {
        List<ReservationResponse> reservations = reservationService.findByMemberId(loginMember.id())
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(ReservationsResponse.from(reservations));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateDateTime(
            @Positive(message = "예약 id는 1 이상의 숫자여야 합니다.")
            @PathVariable Long id,

            @LoginMember LoginMemberInfo loginMember,

            @Valid @RequestBody ReservationUpdateRequest reservationUpdateRequest
    ) {
        Reservation reservation = reservationService.updateDateTime(
                id,
                loginMember.id(),
                reservationUpdateRequest.date(),
                reservationUpdateRequest.timeId()
        );

        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @Positive(message = "예약 id는 1 이상의 숫자여야 합니다.")
            @PathVariable Long id,
            @LoginMember LoginMemberInfo loginMember
    ) {
        reservationService.cancel(id, loginMember.id());
        return ResponseEntity.noContent().build();
    }
}
