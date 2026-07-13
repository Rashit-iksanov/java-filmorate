package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение списка всех пользователей. Количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Получен запрос на создание пользователя с email='{}'", user.getEmail());
        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя не указано, используется логин: '{}'", user.getLogin());
        }

        user.setId(nextId++);
        users.put(user.getId(), user);

        log.info("Пользователь успешно создан с id={}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Получен запрос на обновление пользователя с id={}", user.getId());

        if (!users.containsKey(user.getId())) {
            log.error("Обновление невозможно: пользователь с id={} не найден", user.getId());
            throw new ValidationException("Пользователь с id=" + user.getId() + " не найден");
        }

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);

        log.info("Пользователь с id={} успешно обновлён", user.getId());
        return user;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка валидации: некорректный email '{}'", user.getEmail());
            throw new ValidationException(
                    "Электронная почта не может быть пустой и должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: некорректный логин '{}'", user.getLogin());
            throw new ValidationException(
                    "Логин не может быть пустым и содержать пробелы");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: дата рождения в будущем '{}'", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}