package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

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

    // Друзья

    @Test
    void addFriend_validUsers_shouldAddUnconfirmedFriendships() {
        User user1 = userService.create(createValidUser());

        User user2 = createValidUser();
        user2.setEmail("anna@mail.ru");
        user2.setLogin("anna");
        User createdUser2 = userService.create(user2);

        userService.addFriend(user1.getId(), createdUser2.getId());

        User updatedUser1 = userStorage.findById(user1.getId());
        User updatedUser2 = userStorage.findById(createdUser2.getId());

        Optional<Friendship> friendship1 = updatedUser1.getFriendships().stream()
                .filter(f -> f.getFriendId() == createdUser2.getId()).findFirst();
        Optional<Friendship> friendship2 = updatedUser2.getFriendships().stream()
                .filter(f -> f.getFriendId() == user1.getId()).findFirst();

        assertTrue(friendship1.isPresent());
        assertTrue(friendship2.isPresent());
        assertEquals(FriendStatus.UNCONFIRMED, friendship1.get().getStatus());
        assertEquals(FriendStatus.UNCONFIRMED, friendship2.get().getStatus());
    }

    @Test
    void getCommonFriends_shouldReturnOnlyCommonConfirmedFriends() {
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

        userService.approveFriend(anna.getId(), user1.getId());
        userService.approveFriend(petr.getId(), user1.getId());

        User user4 = createValidUser();
        user4.setEmail("olga@mail.ru");
        user4.setLogin("olga");
        User olga = userService.create(user4);

        userService.addFriend(olga.getId(), anna.getId());
        userService.approveFriend(anna.getId(), olga.getId());

        Collection<User> common = userService.getCommonFriends(user1.getId(), olga.getId());

        assertEquals(1, common.size());
        assertEquals("anna", common.iterator().next().getLogin());
    }

    @Test
    void getFriends_shouldReturnOnlyConfirmedFriends() {
        User user1 = userService.create(createValidUser());
        User user2 = createValidUser();
        user2.setEmail("anna@mail.ru");
        user2.setLogin("anna");
        User createdUser2 = userService.create(user2);

        userService.addFriend(user1.getId(), createdUser2.getId());

        // Пока не подтверждено, друзей быть не должно
        assertTrue(userService.getFriends(user1.getId()).isEmpty());

        // Подтверждаем дружбу
        userService.approveFriend(createdUser2.getId(), user1.getId());

        Collection<User> friends = userService.getFriends(user1.getId());
        assertEquals(1, friends.size());
        assertEquals("anna", friends.iterator().next().getLogin());
    }
}