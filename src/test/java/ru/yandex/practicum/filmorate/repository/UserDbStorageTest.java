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
        // 1. Создаем двух пользователей
        User userA = userStorage.create(createUser("a@mail.ru", "userA", "User A"));
        User userB = userStorage.create(createUser("b@mail.ru", "userB", "User B"));

        // 2. User A отправляет заявку в друзья User B (добавляет в СВОЙ список)
        userStorage.addFriend(userA.getId(), userB.getId());

        // 3. ПРОВЕРКА ПО ТЗ: User A видит User B в своем списке друзей сразу после добавления
        Collection<User> friendsOfA = userStorage.getFriends(userA.getId());
        assertThat(friendsOfA).hasSize(1);
        assertThat(friendsOfA.iterator().next().getLogin()).isEqualTo("userB");

        // 4. ПРОВЕРКА ПО ТЗ: User B НЕ видит User A в своем списке (заявка еще не подтверждена)
        Collection<User> friendsOfB = userStorage.getFriends(userB.getId());
        assertThat(friendsOfB).isEmpty();

        // 5. User B подтверждает дружбу (теперь они должны видеть друг друга)
        userStorage.approveFriend(userB.getId(), userA.getId());

        // 6. ПРОВЕРКА: Теперь User B тоже видит User A в своем списке друзей
        Collection<User> friendsOfBAfterConfirm = userStorage.getFriends(userB.getId());
        assertThat(friendsOfBAfterConfirm).hasSize(1);
        assertThat(friendsOfBAfterConfirm.iterator().next().getLogin()).isEqualTo("userA");

        // 7. ПРОВЕРКА: User A по-прежнему видит User B у себя
        Collection<User> friendsOfAAfterConfirm = userStorage.getFriends(userA.getId());
        assertThat(friendsOfAAfterConfirm).hasSize(1);
        assertThat(friendsOfAAfterConfirm.iterator().next().getLogin()).isEqualTo("userB");
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