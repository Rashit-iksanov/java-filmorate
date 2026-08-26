package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
//@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public Film create(Film film) {
        log.debug("InMemoryFilmStorage.create: получен фильм '{}', текущий nextId={}", film.getName(), nextId);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм '{}' сохранен с id={}. Текущее количество фильмов в хранилище: {}",
                film.getName(), film.getId(), films.size());
        return film;
    }

    @Override
    public Film update(Film film) {
        log.debug("InMemoryFilmStorage.update: попытка обновить фильм с id={}", film.getId());
        if (!films.containsKey(film.getId())) {
            log.warn("Попытка обновить несуществующий фильм с id={}", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("Фильм с id={} обновлен в хранилище", film.getId());
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        log.debug("InMemoryFilmStorage.findAll: возвращаем {} фильмов", films.size());
        return films.values();
    }

    @Override
    public Film findById(int id) {
        log.debug("InMemoryFilmStorage.findById: поиск фильма с id={}", id);
        Film film = films.get(id);
        if (film == null) {
            log.warn("Фильм с id={} не найден в хранилище", id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    @Override
    public void addLike(int filmId, int userId) {

    }

    @Override
    public void removeLike(int filmId, int userId) {

    }

    @Override
    public Collection<Film> getPopular(int count) {
        return List.of();
    }
}