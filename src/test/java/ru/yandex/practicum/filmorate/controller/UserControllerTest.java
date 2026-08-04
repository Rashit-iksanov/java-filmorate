package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        controller = new UserController(userService);
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("ivan@mail.ru");
        user.setLogin("ivan");
        user.setName("Иван Петров");
        user.setBirthday(LocalDate.of(1990, 5, 15));
        return user;
    }

    @Test
    void create_validUser_shouldReturnUserWithId() {
        User user = createValidUser();
        User created = controller.create(user);
        assertNotEquals(0, created.getId());
        assertEquals("ivan@mail.ru", created.getEmail());
    }

    @Test
    void create_emptyName_shouldUseLogin() {
        User user = createValidUser();
        user.setName("");
        User created = controller.create(user);
        assertEquals("ivan", created.getName());
    }

    @Test
    void update_existingUser_shouldUpdate() {
        User user = createValidUser();
        User created = controller.create(user);
        created.setName("Новое имя");
        User updated = controller.update(created);
        assertEquals("Новое имя", updated.getName());
    }

    @Test
    void update_nonExistingUser_shouldThrowNotFoundException() {
        User user = createValidUser();
        user.setId(999);
        assertThrows(NotFoundException.class, () -> controller.update(user));
    }

    @Test
    void findAll_afterCreate_shouldReturnAllUsers() {
        controller.create(createValidUser());
        controller.create(createValidUser());
        assertEquals(2, controller.findAll().size());
    }

    @Test
    void friendWorkflow_addApproveAndGet_shouldReturnFriend() {
        User user1 = controller.create(createValidUser());

        User user2 = createValidUser();
        user2.setEmail("anna@mail.ru");
        user2.setLogin("anna");
        User createdUser2 = controller.create(user2);

        // 1. Добавляем в друзья (статус UNCONFIRMED)
        controller.addFriend(user1.getId(), createdUser2.getId());

        // 2. Подтверждаем дружбу
        controller.approveFriend(createdUser2.getId(), user1.getId());

        Collection<User> friends = controller.getFriends(user1.getId());
        assertEquals(1, friends.size());
        assertEquals("anna", friends.iterator().next().getLogin());
    }
}