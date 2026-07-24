package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

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
    void create_emptyEmail_shouldThrowException() {
        User user = createValidUser();
        user.setEmail("");
        ValidationException ex = assertThrows(ValidationException.class, () -> userService.create(user));
        assertTrue(ex.getMessage().contains("Электронная почта"));
    }

    @Test
    void create_emailWithoutAt_shouldThrowException() {
        User user = createValidUser();
        user.setEmail("ivanmail.ru");
        assertThrows(ValidationException.class, () -> userService.create(user));
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
    void create_birthdayTomorrow_shouldThrowException() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userService.create(user));
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

    // Друзья

    @Test
    void addFriend_validUsers_shouldAddToBothSets() {
        User user1 = userService.create(createValidUser());

        User user2 = createValidUser();
        user2.setEmail("anna@mail.ru");
        user2.setLogin("anna");
        User createdUser2 = userService.create(user2);

        userService.addFriend(user1.getId(), createdUser2.getId());

        Collection<User> friendsOfUser1 = userService.getFriends(user1.getId());
        Collection<User> friendsOfUser2 = userService.getFriends(createdUser2.getId());

        assertEquals(1, friendsOfUser1.size());
        assertEquals(1, friendsOfUser2.size());
        assertTrue(friendsOfUser1.stream().anyMatch(u -> u.getId() == createdUser2.getId()));
    }

    @Test
    void getCommonFriends_shouldReturnOnlyCommon() {
        User user1 = userService.create(createValidUser()); // Иван

        User user2 = createValidUser();
        user2.setEmail("anna@mail.ru");
        user2.setLogin("anna");
        User anna = userService.create(user2);

        User user3 = createValidUser();
        user3.setEmail("petr@mail.ru");
        user3.setLogin("petr");
        User petr = userService.create(user3);

        userService.addFriend(user1.getId(), anna.getId());
        userService.addFriend(user1.getId(), petr.getId());

        User user4 = createValidUser();
        user4.setEmail("olga@mail.ru");
        user4.setLogin("olga");
        User olga = userService.create(user4);
        userService.addFriend(olga.getId(), anna.getId());

        Collection<User> common = userService.getCommonFriends(user1.getId(), olga.getId());

        assertEquals(1, common.size());
        assertEquals("anna", common.iterator().next().getLogin());
    }
}