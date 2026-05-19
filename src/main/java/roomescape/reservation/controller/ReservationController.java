package roomescape.reservation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import roomescape.auth.support.SessionConst;
import roomescape.global.exception.UnauthorizedException;
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
            @Valid @RequestBody ReservationCreateRequest reservationCreateRequest,
            HttpServletRequest request) {
        Long memberId = extractLoginMemberId(request);
        Reservation reservation = reservationService.create(
                memberId,
                reservationCreateRequest.date(),
                reservationCreateRequest.timeId(),
                reservationCreateRequest.themeId()
        );

        return ResponseEntity.status(CREATED)
                .body(ReservationResponse.from(reservation));
    }

    @GetMapping
    public ResponseEntity<ReservationsResponse> list(HttpServletRequest request) {
        Long memberId = extractLoginMemberId(request);
        List<ReservationResponse> reservations = reservationService.findByMemberId(memberId)
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(ReservationsResponse.from(reservations));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateDateTime(
            @Positive(message = "예약 id는 1 이상의 숫자여야 합니다.")
            @PathVariable Long id,

            @Valid @RequestBody ReservationUpdateRequest reservationUpdateRequest,
            HttpServletRequest request
    ) {
        Long memberId = extractLoginMemberId(request);
        Reservation reservation = reservationService.updateDateTime(
                id,
                memberId,
                reservationUpdateRequest.date(),
                reservationUpdateRequest.timeId()
        );

        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @Positive(message = "예약 id는 1 이상의 숫자여야 합니다.")
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long memberId = extractLoginMemberId(request);
        reservationService.cancel(id, memberId);
        return ResponseEntity.noContent().build();
    }

    private Long extractLoginMemberId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        Object memberId = session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        if (memberId instanceof Long id) {
            return id;
        }
        throw new UnauthorizedException("로그인이 필요합니다.");
    }
}
