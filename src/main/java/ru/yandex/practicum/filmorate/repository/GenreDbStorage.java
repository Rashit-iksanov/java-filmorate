package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final String SQL_FIND_ALL_GENRES =
            "SELECT id, name FROM genres ORDER BY id";

    private static final String SQL_FIND_GENRE_BY_ID =
            "SELECT id, name FROM genres WHERE id = ?";

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query(SQL_FIND_ALL_GENRES, genreRowMapper());
    }

    @Override
    public Optional<Genre> findById(int id) {
        List<Genre> genres = jdbcTemplate.query(
                SQL_FIND_GENRE_BY_ID,
                genreRowMapper(),
                id
        );

        return genres.stream().findFirst();
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