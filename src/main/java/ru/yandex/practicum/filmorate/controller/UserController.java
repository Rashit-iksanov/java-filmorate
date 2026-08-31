package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение списка всех пользователей");
        Collection<User> users = userService.findAll();
        log.debug("Возвращено {} пользователей", users.size());
        return users;
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Integer id) {
        log.info("Получен запрос на получение пользователя с id={}", id);
        return userService.findById(id);
    }

    @PostMapping
    public User create(@RequestBody @Valid User user) {
        log.info("Получен запрос на создание пользователя: login='{}', email='{}'", user.getLogin(), user.getEmail());
        User createdUser = userService.create(user);
        log.info("Пользователь успешно создан с id={} и login='{}'", createdUser.getId(), createdUser.getLogin());
        return createdUser;
    }

    @PutMapping
    public User update(@RequestBody @Valid User user) {
        log.info("Получен запрос на обновление пользователя с id={}", user.getId());
        User updatedUser = userService.update(user);
        log.info("Пользователь с id={} успешно обновлён", updatedUser.getId());
        return updatedUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("Получен запрос на добавление в друзья: пользователь {} добавляет пользователя {}", id, friendId);
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("Получен запрос на удаление из друзей: пользователь {} удаляет пользователя {}", id, friendId);
        userService.removeFriend(id, friendId);
        log.info("Пользователь {} успешно удален из друзей у пользователя {}", friendId, id);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable int id) {
        log.info("Получен запрос на получение списка друзей пользователя с id={}", id);
        Collection<User> friends = userService.getFriends(id);
        log.debug("Возвращено {} друзей для пользователя {}", friends.size(), id);
        return friends;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        log.info("Получен запрос на получение общих друзей для пользователей {} и {}", id, otherId);
        Collection<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.debug("Найдено {} общих друзей", commonFriends.size());
        return commonFriends;
    }

    @PutMapping("/{id}/friends/confirm/{friendId}")
    public void approveFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("Получен запрос на подтверждение дружбы: пользователь {} добавляет пользователя {}",
                id, friendId);
        userService.approveFriend(id, friendId);
        log.info("Дружба успешно подтверждена");
    }
}