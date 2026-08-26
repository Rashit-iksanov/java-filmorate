package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public Collection<Genre> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM genres ORDER BY id",
                (rs, rowNum) -> {
                    Genre m = new Genre();
                    m.setId(rs.getInt("id"));
                    m.setName(rs.getString("name"));
                    return m;
                });
    }

    @GetMapping("/{id}")
    public Genre getById(@PathVariable int id) {
        return jdbcTemplate.query("SELECT id, name FROM genres WHERE id = ?",
                        (rs, rowNum) -> {
                            Genre m = new Genre();
                            m.setId(rs.getInt("id"));
                            m.setName(rs.getString("name"));
                            return m;
                        }, id)
                .stream().findFirst().orElseThrow(() -> new NotFoundException("Genre с id " + id + " не найден"));
    }
}
