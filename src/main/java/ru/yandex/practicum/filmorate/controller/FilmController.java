package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;


@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение списка всех фильмов");
        Collection<Film> films = filmService.findAll();
        log.info("Возвращено {} фильмов", films.size());
        return films;
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable int id) {
        log.info("Получен запрос на получение фильма с id={}", id);
        return filmService.findById(id);
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос на создание фильма: name='{}'", film.getName());
        Film createdFilm = filmService.create(film);
        log.info("Фильм успешно создан с id={}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с id={}", film.getId());
        Film updatedFilm = filmService.update(film);
        log.info("Фильм с id={} успешно обновлён", updatedFilm.getId());
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос на добавление лайка: пользователь {} ставит лайк фильму {}", userId, id);
        filmService.addLike(id, userId);
        log.info("Лайк успешно добавлен");
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос на удаление лайка: пользователь {} удаляет лайк у фильма {}", userId, id);
        filmService.removeLike(id, userId);
        log.info("Лайк успешно удален");
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на получение {} популярных фильмов", count);
        Collection<Film> popularFilms = filmService.getPopular(count);
        log.debug("Возвращено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }
}
