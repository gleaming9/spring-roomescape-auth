package roomescape.store.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcStoreRepository implements StoreRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcStoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM store
                    WHERE id = ?
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id));
    }
}
