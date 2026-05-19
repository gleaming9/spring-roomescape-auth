package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ReservationCreateRequest(
        @NotNull(message = "회원 id는 비어 있을 수 없습니다.")
        @Positive(message = "회원 id는 1 이상의 숫자여야 합니다.")
        Long memberId,

        @NotNull(message = "예약 날짜는 비어 있을 수 없습니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @NotNull(message = "예약 시간 id는 비어 있을 수 없습니다.")
        @Positive(message = "예약 시간 id는 1 이상의 숫자여야 합니다.")
        Long timeId,

        @NotNull(message = "테마 id는 비어 있을 수 없습니다.")
        @Positive(message = "테마 id는 1 이상의 숫자여야 합니다.")
        Long themeId
) {
}
