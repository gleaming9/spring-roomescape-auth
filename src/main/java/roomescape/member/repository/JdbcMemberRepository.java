package roomescape.member.repository;

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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

@Repository
public class JdbcMemberRepository implements MemberRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcMemberRepository.class);

    private final RowMapper<Member> memberRowMapper = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            resultSet.getString("name"),
            Role.valueOf(resultSet.getString("role")),
            resultSet.getObject("store_id", Long.class)
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = """
                SELECT id, email, password, name, role, store_id
                FROM member
                WHERE id = ?
                """;

        return jdbcTemplate.query(sql, memberRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = """
                SELECT id, email, password, name, role, store_id
                FROM member
                WHERE email = ?
                """;

        return jdbcTemplate.query(sql, memberRowMapper, email)
                .stream()
                .findFirst();
    }

    @Override
    public Member save(Member member) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowCount = insert(member, keyHolder);
        if (rowCount != 1) {
            log.error("Member insert affected unexpected row count. rowCount={}, email={}", rowCount, member.getEmail());
            throw new InfrastructureException("회원 생성에 실패했습니다.");
        }

        Number key = keyHolder.getKey();
        if (key == null) {
            log.error("Member insert did not return generated id. email={}", member.getEmail());
            throw new InfrastructureException("회원 생성에 실패했습니다.");
        }
        return member.withId(key.longValue());
    }

    private int insert(Member member, KeyHolder keyHolder) {
        String sql = """
                INSERT INTO member (email, password, name, role, store_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setString(1, member.getEmail());
            preparedStatement.setString(2, member.getPassword());
            preparedStatement.setString(3, member.getName());
            preparedStatement.setString(4, member.getRole().name());
            setNullableLong(preparedStatement, 5, member.getStoreId());
            return preparedStatement;
        }, keyHolder);
    }

    private void setNullableLong(PreparedStatement preparedStatement, int parameterIndex, Long value) throws SQLException {
        if (value == null) {
            preparedStatement.setNull(parameterIndex, Types.BIGINT);
            return;
        }

        preparedStatement.setLong(parameterIndex, value);
    }
}
