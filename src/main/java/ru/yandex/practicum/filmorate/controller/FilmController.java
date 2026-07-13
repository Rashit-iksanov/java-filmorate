package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос на создание фильма: name='{}'", film.getName());
        validateFilm(film);

        film.setId(nextId++);
        films.put(film.getId(), film);

        log.info("Фильм успешно создан с id={}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с id={}", film.getId());

        if (!films.containsKey(film.getId())) {
            log.error("Обновление невозможно: фильм с id={} не найден", film.getId());
            throw new ValidationException("Фильм с id=" + film.getId() + " не найден");
        }

        validateFilm(film);
        films.put(film.getId(), film);

        log.info("Фильм с id={} успешно обновлён", film.getId());
        return film;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение списка всех фильмов. Количество: {}", films.size());
        return films.values();
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: название фильма не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: описание фильма превышает 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.error("Ошибка валидации: дата релиза раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: продолжительность фильма неположительная: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
