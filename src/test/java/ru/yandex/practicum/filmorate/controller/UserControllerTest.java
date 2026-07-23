package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("ivan@mail.ru");
        user.setLogin("ivan");
        user.setName("Иван Петров");
        user.setBirthday(LocalDate.of(1990, 5, 15));
        return user;
    }

    // Создание пользователя

    @Test
    void create_validUser_shouldReturnUserWithId() {
        User user = createValidUser();
        User created = controller.create(user);

        assertNotEquals(0, created.getId());
        assertEquals("ivan@mail.ru", created.getEmail());
    }

    @Test
    void create_emptyEmail_shouldThrowException() {
        User user = createValidUser();
        user.setEmail("");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> controller.create(user));
        assertTrue(ex.getMessage().contains("Электронная почта"));
    }

    @Test
    void create_emailWithoutAt_shouldThrowException() {
        User user = createValidUser();
        user.setEmail("ivanmail.ru");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void create_emptyLogin_shouldThrowException() {
        User user = createValidUser();
        user.setLogin("");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void create_loginWithSpaces_shouldThrowException() {
        User user = createValidUser();
        user.setLogin("ivan petrov");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> controller.create(user));
        assertTrue(ex.getMessage().contains("пробелы"));
    }

    @Test
    void create_birthdayTomorrow_shouldThrowException() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void create_birthdayToday_shouldPass() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());  // граничное условие: сегодня

        assertDoesNotThrow(() -> controller.create(user));
    }

    @Test
    void create_emptyName_shouldUseLogin() {
        User user = createValidUser();
        user.setName("");

        User created = controller.create(user);
        assertEquals("ivan", created.getName());
    }

    @Test
    void create_nullName_shouldUseLogin() {
        User user = createValidUser();
        user.setName(null);

        User created = controller.create(user);
        assertEquals("ivan", created.getName());
    }

    // Обновление пользователя

    @Test
    void update_existingUser_shouldUpdate() {
        User user = createValidUser();
        User created = controller.create(user);

        created.setName("Новое имя");
        User updated = controller.update(created);

        assertEquals("Новое имя", updated.getName());
    }

    @Test
    void update_nonExistingUser_shouldThrowException() {
        User user = createValidUser();
        user.setId(999);

        assertThrows(ValidationException.class, () -> controller.update(user));
    }

    // Получение списка

    @Test
    void findAll_emptyStorage_shouldReturnEmptyCollection() {
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void findAll_afterCreate_shouldReturnAllUsers() {
        controller.create(createValidUser());
        controller.create(createValidUser());

        assertEquals(2, controller.findAll().size());
    }
}