package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    List<Reservation> findAll();

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByStoreId(Long storeId);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByStoreIdAndDateAndThemeId(Long storeId, LocalDate date, Long themeId);

    Reservation save(Reservation reservation);

    Optional<Reservation> update(Reservation reservation);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsConflict(Long storeId, LocalDate date, Long timeId, Long themeId);

    boolean existsConflictExcluding(Long storeId, LocalDate date, Long timeId, Long themeId, Long id);

    void deleteById(Long id);
}
