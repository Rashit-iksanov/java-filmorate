package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public Collection<Mpa> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM mpa ORDER BY id",
                (rs, rowNum) -> {
                    Mpa m = new Mpa();
                    m.setId(rs.getInt("id"));
                    m.setName(rs.getString("name"));
                    return m;
                });
    }

    @GetMapping("/{id}")
    public Mpa getById(@PathVariable int id) {
        return jdbcTemplate.query("SELECT id, name FROM mpa WHERE id = ?",
                        (rs, rowNum) -> {
                            Mpa m = new Mpa();
                            m.setId(rs.getInt("id"));
                            m.setName(rs.getString("name"));
                            return m;
                        }, id)
                .stream().findFirst().orElseThrow(() -> new NotFoundException("MPA с id " + id + " не найден"));
    }
}
