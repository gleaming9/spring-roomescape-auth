package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidRequestException;
import roomescape.global.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation create(Long memberId, LocalDate date, Long timeId, Long themeId) {
        Member member = findMember(memberId);
        ReservationTime time = findTime(timeId);
        Theme theme = findTheme(themeId);

        if (reservationRepository.existsConflict(
                date,
                time.getId(),
                theme.getId()
        )) {
            throw new ConflictException("선택한 날짜와 시간에는 이미 해당 테마의 예약이 있습니다. 다른 시간을 선택해주세요.");
        }

        Reservation reservation = Reservation.create(member, date, time, theme, LocalDateTime.now());

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateDateTime(Long id, Long memberId, LocalDate date, Long timeId) {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("변경할 예약이 존재하지 않습니다. 예약 목록을 확인해주세요."));
        if (!reservation.isReservedBy(memberId)) {
            throw new NotFoundException("변경할 예약이 존재하지 않습니다. 예약 목록을 확인해주세요.");
        }

        ReservationTime time = findTime(timeId);

        if (reservationRepository.existsConflictExcluding(
                date,
                time.getId(),
                reservation.getTheme().getId(),
                reservation.getId()
        )) {
            throw new ConflictException("선택한 날짜와 시간에는 이미 해당 테마의 예약이 있습니다. 다른 시간을 선택해주세요.");
        }

        Reservation changedReservation = reservation.changeDateTime(date, time, now);

        return reservationRepository.update(changedReservation)
                .orElseThrow(() -> new NotFoundException("변경할 예약이 존재하지 않습니다. 예약 목록을 확인해주세요."));
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId);
    }

    @Transactional
    public void cancel(Long id, Long memberId) {
        Optional<Reservation> foundReservation = reservationRepository.findById(id);
        if (foundReservation.isEmpty()) {
            return;
        }

        Reservation reservation = foundReservation.get();
        if (!reservation.isReservedBy(memberId)) {
            throw new NotFoundException("취소할 예약이 존재하지 않습니다. 예약 목록을 확인해주세요.");
        }
        if (reservation.isPast(LocalDateTime.now())) {
            throw new InvalidRequestException("이미 지난 예약은 취소할 수 없습니다.");
        }
        reservationRepository.deleteById(reservation.getId());
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    private ReservationTime findTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("선택한 예약 시간이 존재하지 않습니다. 다른 시간을 선택해주세요."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("선택한 테마가 존재하지 않습니다. 다른 테마를 선택해주세요."));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("로그인한 회원을 찾을 수 없습니다. 다시 로그인해주세요."));
    }
}
