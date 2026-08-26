package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Collection<Mpa> findAll() {
        return jdbcTemplate.query("SELECT * FROM mpa ORDER BY id", mpaRowMapper());
    }

    public Optional<Mpa> findById(int id) {
        return jdbcTemplate.query("SELECT * FROM mpa WHERE id = ?", mpaRowMapper(), id).stream().findFirst();
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