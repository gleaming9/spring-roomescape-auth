package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.global.exception.InvalidRequestException;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Reservation {
    private final Long id;
    private final Member member;
    private final Long storeId;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public static Reservation create(Member member,
                                     Long storeId,
                                     LocalDate date,
                                     ReservationTime time,
                                     Theme theme,
                                     LocalDateTime now) {
        Reservation reservation = new Reservation(member, storeId, date, time, theme);
        return validateFutureReservation(reservation, now);
    }

    private static Reservation validateFutureReservation(Reservation reservation, LocalDateTime now) {
        if (reservation.isPast(now)) {
            throw new InvalidRequestException("현재 시각 이후의 날짜와 시간을 선택해주세요.");
        }

        return reservation;
    }

    public Reservation(Member member, Long storeId, LocalDate date, ReservationTime time, Theme theme) {
        this(null, member, storeId, date, time, theme);
    }

    public Reservation changeDateTime(LocalDate newDate, ReservationTime newTime, LocalDateTime now) {
        if (isPast(now)) {
            throw new InvalidRequestException("이미 지난 예약은 변경할 수 없습니다.");
        }

        if (Objects.equals(this.date, newDate)
                && Objects.equals(this.time.getStartAt(), newTime.getStartAt())) {
            throw new InvalidRequestException("변경할 날짜와 시간을 현재 예약과 다르게 선택해주세요.");
        }

        Reservation changedReservation = new Reservation(id, member, storeId, newDate, newTime, theme);
        if (changedReservation.isPast(now)) {
            throw new InvalidRequestException("현재 시각 이후의 날짜와 시간을 선택해주세요.");
        }

        return changedReservation;
    }

    public Reservation(Long id, Member member, Long storeId, LocalDate date, ReservationTime time, Theme theme) {
        validate(member, storeId, date, time, theme);
        this.id = id;
        this.member = member;
        this.storeId = storeId;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validate(Member member, Long storeId, LocalDate date, ReservationTime time, Theme theme) {
        validateMember(member);
        validateStoreId(storeId);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new InvalidRequestException("예약 회원은 비어 있을 수 없습니다.");
        }
    }

    private void validateStoreId(Long storeId) {
        if (storeId == null) {
            throw new InvalidRequestException("예약 매장은 비어 있을 수 없습니다.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new InvalidRequestException("예약 날짜는 비어 있을 수 없습니다.");
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new InvalidRequestException("예약 시간은 비어 있을 수 없습니다.");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new InvalidRequestException("테마 정보는 비어 있을 수 없습니다.");
        }
    }

    public Reservation withId(Long id) {
        validateId(id);

        if (this.id != null) {
            throw new InvalidRequestException("이미 식별자가 존재하는 예약입니다.");
        }

        return new Reservation(id, member, storeId, date, time, theme);
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new InvalidRequestException("예약 id는 비어 있을 수 없습니다.");
        }
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public boolean isReservedBy(Long memberId) {
        return member != null && Objects.equals(member.getId(), memberId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

