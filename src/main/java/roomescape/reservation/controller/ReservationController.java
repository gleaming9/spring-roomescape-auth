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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
            @Valid @RequestBody ReservationCreateRequest reservationCreateRequest) {
        Reservation reservation = reservationService.create(
                reservationCreateRequest.memberId(),
                reservationCreateRequest.date(),
                reservationCreateRequest.timeId(),
                reservationCreateRequest.themeId()
        );

        return ResponseEntity.status(CREATED)
                .body(ReservationResponse.from(reservation));
    }

    @GetMapping
    public ResponseEntity<ReservationsResponse> list(
            @Positive(message = "회원 id는 1 이상의 숫자여야 합니다.")
            @RequestParam Long memberId
    ) {
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

            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        Reservation reservation = reservationService.updateDateTime(
                id,
                request.memberId(),
                request.date(),
                request.timeId()
        );

        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @Positive(message = "예약 id는 1 이상의 숫자여야 합니다.")
            @PathVariable Long id,
            @Positive(message = "회원 id는 1 이상의 숫자여야 합니다.")
            @RequestParam Long memberId
    ) {
        reservationService.cancel(id, memberId);
        return ResponseEntity.noContent().build();
    }
}
