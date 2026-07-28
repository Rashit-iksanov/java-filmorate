package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Film create(Film film) {
        log.info("Создание фильма: name='{}'", film.getName());
        validateFilm(film);
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

        Film film = filmStorage.findById(filmId);

        userStorage.findById(userId);

        film.getLikes().add(Long.valueOf(userId));
        filmStorage.update(film);
        log.info("Лайк успешно добавлен");
    }

    public void removeLike(int filmId, int userId) {
        log.info("Пользователь {} удаляет лайк у фильма {}", userId, filmId);

        Film film = filmStorage.findById(filmId);

        userStorage.findById(userId);

        film.getLikes().remove(Long.valueOf(userId));
        filmStorage.update(film);
        log.info("Лайк успешно удален");
    }

    public Collection<Film> getPopular(int count) {
        log.info("Запрошены {} самых популярных фильмов", count);
        Collection<Film> allFilms = filmStorage.findAll();
        log.debug("Всего фильмов в базе для сортировки: {}", allFilms.size());

        Collection<Film> popularFilms = allFilms.stream()
                .sorted((f1, f2) -> {
                    int cmp = Integer.compare(f2.getLikes().size(), f1.getLikes().size());
                    log.trace("Сравнение фильмов: '{}' ({} лайков) и '{}' ({} лайков) -> результат {}",
                            f1.getName(), f1.getLikes().size(), f2.getName(), f2.getLikes().size(), cmp);
                    return cmp;
                })
                .limit(count)
                .collect(Collectors.toList());

        log.info("Возвращаем {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    private void validateFilm(Film film) {
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