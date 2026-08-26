package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreDbStorage;
import ru.yandex.practicum.filmorate.repository.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Film create(Film film) {
        log.info("Создание фильма: name='{}'", film.getName());
        validateFilm(film);

        // ПРОВЕРКА СУЩЕСТВОВАНИЯ MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaDbStorage.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден"));
        }

        // ПРОВЕРКА СУЩЕСТВОВАНИЯ ЖАНРОВ
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                genreDbStorage.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id=" + genre.getId() + " не найден"));
            }
        }
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.info("Обновление фильма с id={}", film.getId());
        validateFilm(film);
        return filmStorage.update(film);
    }

    public Collection<Film> findAll() {
        log.info("Получение списка всех фильмов");
        return filmStorage.findAll();
    }

    public Film findById(int id) {
        log.info("Получение фильма по id={}", id);
        return filmStorage.findById(id);
    }

    public void addLike(int filmId, int userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, filmId);
        filmStorage.findById(filmId);
        userStorage.findById(userId);

        filmStorage.addLike(filmId, userId);
        log.info("Лайк успешно добавлен");
    }

    public void removeLike(int filmId, int userId) {
        log.info("Пользователь {} удаляет лайк у фильма {}", userId, filmId);
        filmStorage.findById(filmId);
        userStorage.findById(userId);

        filmStorage.removeLike(filmId, userId);
        log.info("Лайк успешно удален");
    }

    public Collection<Film> getPopular(int count) {
        log.info("Запрошены {} самых популярных фильмов", count);
        return filmStorage.getPopular(count);
    }

    private void validateFilm(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("Рейтинг MPA должен быть указан");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}