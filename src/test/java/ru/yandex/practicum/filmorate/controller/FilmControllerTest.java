package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;
    private UserController userController; // <-- Добавляем
    private FilmService filmService;


    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();

        filmService = new FilmService(filmStorage, userStorage);
        UserService userService = new UserService(userStorage);

        filmController = new FilmController(filmService);
        userController = new UserController(userService);
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Матрица");
        film.setDescription("Фантастический боевик");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);

        Mpa mpa = new Mpa();
        mpa.setId(5);
        mpa.setName("NC-17");
        film.setMpa(mpa);

        Set<Genre> genres = new HashSet<>();
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Боевик");
        genres.add(genre);
        film.setGenres(genres);
        return film;
    }

    //Создание фильма

    @Test
    void create_validFilm_shouldReturnFilmWithId() {
        Film film = createValidFilm();
        Film created = filmController.create(film);

        assertNotEquals(0, created.getId());
        assertEquals("Матрица", created.getName());
    }

    @Test
    void create_invalidFilm_shouldThrowExceptionFromService() {
        Film film = createValidFilm();
        film.setName("");

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void findAll_emptyStorage_shouldReturnEmptyCollection() {
        Collection<Film> films = filmController.findAll();
        assertTrue(films.isEmpty());
    }

    @Test
    void findAll_afterCreate_shouldReturnAllFilms() {
        filmController.create(createValidFilm());
        filmController.create(createValidFilm());

        Collection<Film> films = filmController.findAll();
        assertEquals(2, films.size());
    }
}