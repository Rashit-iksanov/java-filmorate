package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
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
        User created = userService.create(user);
        assertNotEquals(0, created.getId());
        assertEquals("ivan@mail.ru", created.getEmail());
    }

    @Test
    void create_emptyLogin_shouldThrowException() {
        User user = createValidUser();
        user.setLogin("");
        assertThrows(ValidationException.class, () -> userService.create(user));
    }

    @Test
    void create_loginWithSpaces_shouldThrowException() {
        User user = createValidUser();
        user.setLogin("ivan petrov");
        ValidationException ex = assertThrows(ValidationException.class, () -> userService.create(user));
        assertTrue(ex.getMessage().contains("пробелы"));
    }

    @Test
    void create_birthdayToday_shouldPass() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());
        assertDoesNotThrow(() -> userService.create(user));
    }

    @Test
    void create_emptyName_shouldUseLogin() {
        User user = createValidUser();
        user.setName("");
        User created = userService.create(user);
        assertEquals("ivan", created.getName());
    }

    @Test
    void create_nullName_shouldUseLogin() {
        User user = createValidUser();
        user.setName(null);
        User created = userService.create(user);
        assertEquals("ivan", created.getName());
    }

    // Обновление пользователя

    @Test
    void update_existingUser_shouldUpdate() {
        User user = createValidUser();
        User created = userService.create(user);
        created.setName("Новое имя");
        User updated = userService.update(created);
        assertEquals("Новое имя", updated.getName());
    }

    @Test
    void update_nonExistingUser_shouldThrowNotFoundException() {
        User user = createValidUser();
        user.setId(999);
        assertThrows(NotFoundException.class, () -> userService.update(user));
    }
}