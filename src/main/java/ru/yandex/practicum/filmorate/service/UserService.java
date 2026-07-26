package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
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

        log.debug("Друзья user {} ДО: {}", userId, user.getFriends());
        log.debug("Друзья friend {} ДО: {}", friendId, friend.getFriends());

        boolean added1 = user.getFriends().add(Long.valueOf(friendId));
        boolean added2 = friend.getFriends().add(Long.valueOf(userId));

        log.debug("Результат добавления (user->friend): {}, (friend->user): {}", added1, added2);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Дружба установлена. Друзья user {} ПОСЛЕ: {}", userId, user.getFriends());
    }

    public void removeFriend(int userId, int friendId) {
        log.info("Начало операции удаления из друзей: user {} -> friend {}", userId, friendId);
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        boolean removed1 = user.getFriends().remove(Long.valueOf(friendId));
        boolean removed2 = friend.getFriends().remove(Long.valueOf(userId));

        log.debug("Результат удаления: user={}, friend={}", removed1, removed2);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public Collection<User> getFriends(int userId) {
        log.info("Получение списка друзей пользователя {}", userId);
        User user = userStorage.findById(userId);

        Collection<Integer> friendIds = user.getFriends().stream()
                .map(Long::intValue)
                .collect(Collectors.toList());

        return userStorage.findByIds(friendIds);
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        log.info("Поиск общих друзей для пользователей {} и {}", userId, otherId);
        User user = userStorage.findById(userId);
        User other = userStorage.findById(otherId);

        log.debug("Друзья user {}: {}", userId, user.getFriends());
        log.debug("Друзья other {}: {}", otherId, other.getFriends());

        Collection<Integer> commonFriendIds = user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(Long::intValue)
                .collect(Collectors.toList());

        log.debug("Найдено {} общих ID друзей", commonFriendIds.size());

        Collection<User> commonFriends = userStorage.findByIds(commonFriendIds);

        log.info("Найдено {} общих друзей", commonFriends.size());
        return commonFriends;
    }

    private void validateUser(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
    }
}