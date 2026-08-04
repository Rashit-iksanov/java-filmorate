package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        log.info("Создание пользователя с email='{}'", user.getEmail());
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя не указано, используется логин: '{}'", user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        log.info("Обновление пользователя с id={}", user.getId());
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    public Collection<User> findAll() {
        log.info("Получение списка всех пользователей");
        return userStorage.findAll();
    }

    public User findById(int id) {
        log.info("Получение пользователя по id={}", id);
        return userStorage.findById(id);
    }

    public void addFriend(int userId, int friendId) {
        log.info("Начало операции добавления в друзья: user {} -> friend {}", userId, friendId);
        if (userId == friendId) {
            log.warn("Попытка добавить пользователя {} в друзья самому себе", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        // Создаем запись для пользователя (он отправил запрос)
        Friendship userToFriend = new Friendship();
        userToFriend.setUserId(userId);
        userToFriend.setFriendId(friendId);
        userToFriend.setStatus(FriendStatus.UNCONFIRMED);

        // Создаем запись для друга (к нему пришел запрос)
        Friendship friendToUser = new Friendship();
        friendToUser.setUserId(friendId);
        friendToUser.setFriendId(userId);
        friendToUser.setStatus(FriendStatus.UNCONFIRMED);

        // Добавляем в множества
        user.getFriendships().add(userToFriend);
        friend.getFriendships().add(friendToUser);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Запрос в друзья отправлен");
    }

    public void approveFriend(int userId, int friendId) {
        log.info("Подтверждение дружбы: user {} подтверждает friend {}", userId, friendId);
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        // 1. Находим запись у пользователя, который подтверждает, и меняем статус
        user.getFriendships().stream()
                .filter(f -> f.getFriendId() == friendId)
                .findFirst()
                .ifPresent(f -> f.setStatus(FriendStatus.CONFIRMED));

        // 2. ВАЖНО: Находим запись у друга и ТОЖЕ меняем статус на подтвержденный (взаимная дружба)
        friend.getFriendships().stream()
                .filter(f -> f.getFriendId() == userId)
                .findFirst()
                .ifPresent(f -> f.setStatus(FriendStatus.CONFIRMED));

        userStorage.update(user);
        userStorage.update(friend);
        log.info("Дружба успешно подтверждена с обеих сторон");
    }

    public void removeFriend(int userId, int friendId) {
        log.info("Начало операции удаления из друзей: user {} -> friend {}", userId, friendId);
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        boolean removed1 = user.getFriendships().removeIf(f -> f.getFriendId() == friendId);
        boolean removed2 = friend.getFriendships().removeIf(f -> f.getFriendId() == userId);

        log.debug("Результат удаления: user={}, friend={}", removed1, removed2);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public Collection<User> getFriends(int userId) {
        log.info("Получение списка друзей пользователя {}", userId);
        User user = userStorage.findById(userId);

        // Собираем ID только тех друзей, у которых статус CONFIRMED
        Collection<Integer> confirmedFriendIds = user.getFriendships().stream()
                .filter(f -> f.getStatus() == FriendStatus.CONFIRMED)
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());

        log.debug("Найдено подтвержденных друзей с ID: {}", confirmedFriendIds);
        return userStorage.findByIds(confirmedFriendIds);
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        log.info("Поиск общих друзей для пользователей {} и {}", userId, otherId);
        User user = userStorage.findById(userId);
        User other = userStorage.findById(otherId);

        log.debug("Друзья user {}: {}", userId, user.getFriendships());
        log.debug("Друзья other {}: {}", otherId, other.getFriendships());

        Collection<Integer> userFriendIds = user.getFriendships().stream()
                .filter(f -> f.getStatus() == FriendStatus.CONFIRMED)
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());

        Collection<Integer> otherFriendIds = other.getFriendships().stream()
                .filter(f -> f.getStatus() == FriendStatus.CONFIRMED)
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());

        userFriendIds.retainAll(otherFriendIds);

        Collection<User> commonFriends = userStorage.findByIds(userFriendIds);

        log.info("Найдено {} общих друзей для пользователей {} и {}", commonFriends.size(), userId, otherId);
        return commonFriends;
    }

    private void validateUser(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
    }
}