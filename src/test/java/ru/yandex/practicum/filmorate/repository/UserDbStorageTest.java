package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class) // <-- ЯВНО УКАЗЫВАЕМ, ЧТО НУЖНО ЗАГРУЗИТЬ ЭТОТ БИН
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void testCreateAndFindUser() {
        User user = createUser("test@mail.ru", "testlogin", "Test User");
        User created = userStorage.create(user);

        assertThat(created.getId()).isGreaterThan(0);

        User found = userStorage.findById(created.getId());
        assertThat(found.getEmail()).isEqualTo("test@mail.ru");
    }

    @Test
    void testUpdateUser() {
        User user = userStorage.create(createUser("update@mail.ru", "updatelogin", "Old Name"));
        user.setName("New Name");

        User updated = userStorage.update(user);
        assertThat(updated.getName()).isEqualTo("New Name");

        assertThat(userStorage.findById(user.getId()).getName()).isEqualTo("New Name");
    }

    @Test
    void testFindAllUsers() {
        userStorage.create(createUser("u1@mail.ru", "u1", "User 1"));
        userStorage.create(createUser("u2@mail.ru", "u2", "User 2"));

        Collection<User> users = userStorage.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void testOneWayFriendship() {
        User userA = userStorage.create(createUser("a@mail.ru", "userA", "User A"));
        User userB = userStorage.create(createUser("b@mail.ru", "userB", "User B"));

        // 1. User A отправляет заявку User B
        userStorage.addFriend(userA.getId(), userB.getId());

        // У User A друзей пока нет (статус unconfirmed)
        assertThat(userStorage.getFriends(userA.getId())).isEmpty();
        // У User B друзей тоже нет
        assertThat(userStorage.getFriends(userB.getId())).isEmpty();

        // 2. User B подтверждает заявку User A
        userStorage.approveFriend(userB.getId(), userA.getId());

        // 3. Проверяем односторонность:
        // У User A теперь есть друг User B (потому что A отправлял заявку, и она подтверждена)
        Collection<User> friendsOfA = userStorage.getFriends(userA.getId());
        assertThat(friendsOfA).hasSize(1);
        assertThat(friendsOfA.iterator().next().getLogin()).isEqualTo("userB");

        // У User B НЕТ друга User A (потому что B не отправлял заявку А)
        Collection<User> friendsOfB = userStorage.getFriends(userB.getId());
        assertThat(friendsOfB).isEmpty();
    }

    @Test
    void testCommonFriends() {
        User userA = userStorage.create(createUser("a@mail.ru", "userA", "User A"));
        User userB = userStorage.create(createUser("b@mail.ru", "userB", "User B"));
        User userC = userStorage.create(createUser("c@mail.ru", "userC", "User C"));

        // A и B дружат с C
        userStorage.addFriend(userA.getId(), userC.getId());
        userStorage.approveFriend(userC.getId(), userA.getId()); // C подтверждает A

        userStorage.addFriend(userB.getId(), userC.getId());
        userStorage.approveFriend(userC.getId(), userB.getId()); // C подтверждает B

        Collection<User> common = userStorage.getCommonFriends(userA.getId(), userB.getId());
        assertThat(common).hasSize(1);
        assertThat(common.iterator().next().getLogin()).isEqualTo("userC");
    }

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}