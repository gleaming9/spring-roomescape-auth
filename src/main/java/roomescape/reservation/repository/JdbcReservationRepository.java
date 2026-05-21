package roomescape.reservation.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.InfrastructureException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationRepository implements ReservationRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcReservationRepository.class);

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        Member member = new Member(
                resultSet.getLong("member_id"),
                resultSet.getString("member_email"),
                resultSet.getString("member_password"),
                resultSet.getString("member_name"),
                Role.valueOf(resultSet.getString("member_role")),
                resultSet.getObject("member_store_id", Long.class)
        );

        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail")
        );

        return new Reservation(
                resultSet.getLong("reservation_id"),
                member,
                resultSet.getLong("store_id"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.store_id,
                    r.date,
                    m.id AS member_id,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.name AS member_name,
                    m.role AS member_role,
                    m.store_id AS member_store_id,
                    t.id AS time_id,
                    t.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation r
                INNER JOIN member m
                    ON r.member_id = m.id
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                """;

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public List<Reservation> findByMemberId(Long memberId) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.store_id,
                    r.date,
                    m.id AS member_id,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.name AS member_name,
                    m.role AS member_role,
                    m.store_id AS member_store_id,
                    t.id AS time_id,
                    t.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation r
                INNER JOIN member m
                    ON r.member_id = m.id
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.member_id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, memberId);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.store_id,
                    r.date,
                    m.id AS member_id,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.name AS member_name,
                    m.role AS member_role,
                    m.store_id AS member_store_id,
                    t.id AS time_id,
                    t.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation r
                INNER JOIN member m
                    ON r.member_id = m.id
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Reservation> findByStoreIdAndDateAndThemeId(Long storeId, LocalDate date, Long themeId) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.store_id,
                    r.date,
                    m.id AS member_id,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.name AS member_name,
                    m.role AS member_role,
                    m.store_id AS member_store_id,
                    t.id AS time_id,
                    t.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation r
                INNER JOIN member m
                    ON r.member_id = m.id
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.store_id = ? AND r.date = ? AND r.theme_id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, storeId, date, themeId);
    }

    @Override
    public Reservation save(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowCount = insert(reservation, keyHolder);
        validateCreatedRowCount(rowCount, reservation);

        Long id = getGeneratedId(keyHolder, reservation);
        return reservation.withId(id);
    }

    @Override
    public Optional<Reservation> update(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET date = ?, time_id = ?
                WHERE id = ?
                """;

        int updatedRowCount = jdbcTemplate.update(
                sql,
                Date.valueOf(reservation.getDate()),
                reservation.getTime().getId(),
                reservation.getId()
        );

        if (updatedRowCount == 0) {
            return Optional.empty();
        }

        return Optional.of(reservation);
    }

    private int insert(Reservation reservation, KeyHolder keyHolder) {
        String sql = """
                INSERT INTO reservation (member_id, store_id, date, time_id, theme_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            preparedStatement.setLong(1, reservation.getMember().getId());
            preparedStatement.setLong(2, reservation.getStoreId());
            preparedStatement.setDate(3, Date.valueOf(reservation.getDate()));
            preparedStatement.setLong(4, reservation.getTime().getId());
            preparedStatement.setLong(5, reservation.getTheme().getId());
            return preparedStatement;
        }, keyHolder);
    }

    private void validateCreatedRowCount(int rowCount, Reservation reservation) {
        if (rowCount != 1) {
            log.error(
                    "Reservation insert affected unexpected row count. rowCount={}, memberId={}, date={}, timeId={}, themeId={}",
                    rowCount,
                    reservation.getMember().getId(),
                    reservation.getDate(),
                    reservation.getTime().getId(),
                    reservation.getTheme().getId()
            );
            throw new InfrastructureException("예약 생성에 실패했습니다.");
        }
    }

    private Long getGeneratedId(KeyHolder keyHolder, Reservation reservation) {
        Number key = keyHolder.getKey();
        if (key == null) {
            log.error(
                    "Reservation insert did not return generated id. memberId={}, date={}, timeId={}, themeId={}",
                    reservation.getMember().getId(),
                    reservation.getDate(),
                    reservation.getTime().getId(),
                    reservation.getTheme().getId()
            );
            throw new InfrastructureException("예약 생성에 실패했습니다.");
        }
        return key.longValue();
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE time_id = ?
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, timeId));
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE theme_id = ?
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, themeId));
    }

    @Override
    public boolean existsConflict(Long storeId, LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE store_id = ? AND date = ? AND time_id = ? AND theme_id = ?
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, storeId, date, timeId, themeId));
    }

    @Override
    public boolean existsConflictExcluding(Long storeId, LocalDate date, Long timeId, Long themeId, Long id) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE store_id = ? AND date = ? AND time_id = ? AND theme_id = ? AND id != ?
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, storeId, date, timeId, themeId, id));
    }

    @Override
    public void deleteById(Long id) {
        String sql = """
                DELETE FROM reservation
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, id);
    }
}
