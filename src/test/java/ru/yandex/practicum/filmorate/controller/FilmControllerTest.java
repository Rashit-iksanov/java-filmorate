package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
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
        Film created = controller.create(film);

        assertNotEquals(0, created.getId());
        assertEquals("Матрица", created.getName());
    }

    @Test
    void create_emptyName_shouldThrowException() {
        Film film = createValidFilm();
        film.setName("");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> controller.create(film));
        assertTrue(ex.getMessage().contains("Название не может быть пустым"));
    }

    @Test
    void create_nullName_shouldThrowException() {
        Film film = createValidFilm();
        film.setName(null);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void create_descriptionExactly200Chars_shouldPass() {
        Film film = createValidFilm();
        film.setDescription("A".repeat(200));  // граничное условие: ровно 200

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void create_description201Chars_shouldThrowException() {
        Film film = createValidFilm();
        film.setDescription("A".repeat(201));  // граничное условие: 201

        ValidationException ex = assertThrows(ValidationException.class,
                () -> controller.create(film));
        assertTrue(ex.getMessage().contains("200 символов"));
    }

    @Test
    void create_releaseDateExactly1895Dec28_shouldPass() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));  // граничное условие

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void create_releaseDateOneDayBefore_shouldThrowException() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void create_durationOne_shouldPass() {
        Film film = createValidFilm();
        film.setDuration(1);  // граничное условие

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void create_durationZero_shouldThrowException() {
        Film film = createValidFilm();
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void create_negativeDuration_shouldThrowException() {
        Film film = createValidFilm();
        film.setDuration(-10);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    // Обновление фильма

    @Test
    void update_existingFilm_shouldUpdate() {
        Film film = createValidFilm();
        Film created = controller.create(film);

        created.setName("Матрица: Перезагрузка");
        Film updated = controller.update(created);

        assertEquals("Матрица: Перезагрузка", updated.getName());
    }

    @Test
    void update_nonExistingFilm_shouldThrowException() {
        Film film = createValidFilm();
        film.setId(999);

        assertThrows(ValidationException.class, () -> controller.update(film));
    }

    // Получение списка

    @Test
    void findAll_emptyStorage_shouldReturnEmptyCollection() {
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void findAll_afterCreate_shouldReturnAllFilms() {
        controller.create(createValidFilm());
        controller.create(createValidFilm());

        assertEquals(2, controller.findAll().size());
    }
}