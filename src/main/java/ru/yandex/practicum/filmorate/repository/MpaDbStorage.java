package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaRepository {

    private static final String SQL_FIND_ALL_MPA =
            "SELECT id, name FROM mpa ORDER BY id";

    private static final String SQL_FIND_MPA_BY_ID =
            "SELECT id, name FROM mpa WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> findAll() {
        return jdbcTemplate.query(SQL_FIND_ALL_MPA, mpaRowMapper());
    }

    @Override
    public Optional<Mpa> findById(int id) {
        List<Mpa> result = jdbcTemplate.query(SQL_FIND_MPA_BY_ID, mpaRowMapper(), id);
        return result.stream().findFirst();
    }

    private RowMapper<Mpa> mpaRowMapper() {
        return (rs, rowNum) -> {
            Mpa m = new Mpa();
            m.setId(rs.getInt("id"));
            m.setName(rs.getString("name"));
            return m;
        };
    }
}