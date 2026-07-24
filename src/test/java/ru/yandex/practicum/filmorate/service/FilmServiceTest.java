package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    private FilmService filmService;
    private FilmStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        filmService = new FilmService(filmStorage);
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Матрица");
        film.setDescription("Фантастический боевик");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
        return film;
    }

    // Создание фильма

    @Test
    void create_validFilm_shouldReturnFilmWithId() {
        Film film = createValidFilm();
        Film created = filmService.create(film);

        assertNotEquals(0, created.getId());
        assertEquals("Матрица", created.getName());
    }

    @Test
    void create_emptyName_shouldThrowException() {
        Film film = createValidFilm();
        film.setName("");
        ValidationException ex = assertThrows(ValidationException.class, () -> filmService.create(film));
        assertTrue(ex.getMessage().contains("Название не может быть пустым"));
    }

    @Test
    void create_nullName_shouldThrowException() {
        Film film = createValidFilm();
        film.setName(null);
        assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    void create_descriptionExactly200Chars_shouldPass() {
        Film film = createValidFilm();
        film.setDescription("A".repeat(200));
        assertDoesNotThrow(() -> filmService.create(film));
    }

    @Test
    void create_description201Chars_shouldThrowException() {
        Film film = createValidFilm();
        film.setDescription("A".repeat(201));
        ValidationException ex = assertThrows(ValidationException.class, () -> filmService.create(film));
        assertTrue(ex.getMessage().contains("200 символов"));
    }

    @Test
    void create_releaseDateExactly1895Dec28_shouldPass() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertDoesNotThrow(() -> filmService.create(film));
    }

    @Test
    void create_releaseDateOneDayBefore_shouldThrowException() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    void create_durationOne_shouldPass() {
        Film film = createValidFilm();
        film.setDuration(1);
        assertDoesNotThrow(() -> filmService.create(film));
    }

    @Test
    void create_durationZero_shouldThrowException() {
        Film film = createValidFilm();
        film.setDuration(0);
        assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    void create_negativeDuration_shouldThrowException() {
        Film film = createValidFilm();
        film.setDuration(-10);
        assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    // Обновление фильма

    @Test
    void update_existingFilm_shouldUpdate() {
        Film film = createValidFilm();
        Film created = filmService.create(film);

        created.setName("Матрица: Перезагрузка");
        Film updated = filmService.update(created);

        assertEquals("Матрица: Перезагрузка", updated.getName());
    }

    @Test
    void update_nonExistingFilm_shouldThrowNotFoundException() {
        Film film = createValidFilm();
        film.setId(999); // ID, которого нет в хранилище
        assertThrows(NotFoundException.class, () -> filmService.update(film));
    }

    // Получение списка

    @Test
    void findAll_emptyStorage_shouldReturnEmptyCollection() {
        assertTrue(filmService.findAll().isEmpty());
    }

    @Test
    void findAll_afterCreate_shouldReturnAllFilms() {
        filmService.create(createValidFilm());
        filmService.create(createValidFilm());
        assertEquals(2, filmService.findAll().size());
    }

    // Лайки и Популярное

    @Test
    void addLike_shouldAddUserIdToLikesSet() {
        Film film = filmService.create(createValidFilm());
        filmService.addLike(film.getId(), 42);

        assertTrue(film.getLikes().contains(42L));
        assertEquals(1, film.getLikes().size());
    }

    @Test
    void getPopular_shouldReturnSortedFilmsByLikes() {
        Film film1 = filmService.create(createValidFilm());
        film1.setName("Популярный");
        filmService.update(film1);

        Film film2 = filmService.create(createValidFilm());
        film2.setName("Непопулярный");
        filmService.update(film2);

        filmService.addLike(film1.getId(), 1);
        filmService.addLike(film1.getId(), 2);
        filmService.addLike(film2.getId(), 3);

        Collection<Film> popular = filmService.getPopular(1);

        assertEquals(1, popular.size());
        assertEquals("Популярный", popular.iterator().next().getName());
    }
}