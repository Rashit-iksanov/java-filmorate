package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Collection<Film> findAll();

    Film findById(int id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    Collection<Film> getPopular(int count);
}