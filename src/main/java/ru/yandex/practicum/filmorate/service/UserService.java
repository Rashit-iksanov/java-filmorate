package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public User create(User user) {
        log.info("Создание пользователя с email='{}'", user.getEmail());
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя не указано, используется логин: '{}'", user.getLogin());
        }
        User createdUser = userStorage.create(user);
        log.info("Пользователь успешно создан с id={}", createdUser.getId());
        return createdUser;
    }

    public User update(User user) {
        log.info("Обновление пользователя с id={}", user.getId());
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User updatedUser = userStorage.update(user);
        log.info("Пользователь с id={} успешно обновлён", updatedUser.getId());
        return updatedUser;
    }

    public Collection<User> findAll() {
        log.info("Получение списка всех пользователей");
        return userStorage.findAll();
    }

    public User findById(int id) {
        log.info("Получение пользователя по id={}", id);
        return userStorage.findById(id);
    }

    public User addFriend(int userId, int friendId) {
        log.info("Начало операции добавления в друзья: user {} -> friend {}", userId, friendId);
        if (userId == friendId) {
            log.warn("Попытка добавить пользователя {} в друзья самому себе", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        userStorage.findById(userId);
        userStorage.findById(friendId);
        userStorage.addFriend(userId, friendId); // Односторонняя запись!

        return userStorage.findById(userId);
    }

    public void approveFriend(int userId, int friendId) {
        log.info("Подтверждение дружбы: user {} подтверждает friend {}", userId, friendId);
        userStorage.findById(userId);
        userStorage.findById(friendId);
        userStorage.approveFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        log.info("Запрос на удаление из друзей: пользователь {} удаляет пользователя {}", userId, friendId);

        userStorage.findById(userId);
        userStorage.findById(friendId);

        // Удаляем связь в обе стороны, на случай если дружба уже была подтверждена
        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь успешно удален из друзей");
    }

    public Collection<User> getFriends(int userId) {
        log.info("Запрос на получение списка друзей пользователя с id={}", userId);
        userStorage.findById(userId); // Проверка существования пользователя
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        log.info("Запрос на получение общих друзей для пользователей {} и {}", userId, otherId);

        userStorage.findById(userId);
        userStorage.findById(otherId);

        return userStorage.getCommonFriends(userId, otherId);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(java.time.LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}