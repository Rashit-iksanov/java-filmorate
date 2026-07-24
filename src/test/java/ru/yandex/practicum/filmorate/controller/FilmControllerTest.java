package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        filmService = new FilmService(filmStorage);
        filmController = new FilmController(filmService);
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Матрица");
        film.setDescription("Фантастический боевик");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
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

    @Test
    void addLike_shouldDelegateToService() {
        Film film = filmController.create(createValidFilm());
        int userId = 42;
        filmController.addLike(film.getId(), userId);

        Film updatedFilm = filmController.findById(film.getId());
        assertTrue(updatedFilm.getLikes().contains((long) userId));
    }
}