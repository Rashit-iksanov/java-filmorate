package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Collection<Genre> findAll() {
        return jdbcTemplate.query("SELECT * FROM genres ORDER BY id", genreRowMapper());
    }

    public Optional<Genre> findById(int id) {
        return jdbcTemplate.query("SELECT * FROM genres WHERE id = ?", genreRowMapper(), id).stream().findFirst();
    }

    private RowMapper<Genre> genreRowMapper() {
        return (rs, rowNum) -> {
            Genre g = new Genre();
            g.setId(rs.getInt("id"));
            g.setName(rs.getString("name"));
            return g;
        };
    }
}