package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
//@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User create(User user) {
        log.debug("InMemoryUserStorage.create: получен пользователь '{}', текущий nextId={}", user.getLogin(), nextId);
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь '{}' сохранен с id={}. Текущее количество пользователей: {}",
                user.getLogin(), user.getId(), users.size());
        return user;
    }

    @Override
    public User update(User user) {
        log.debug("InMemoryUserStorage.update: попытка обновить пользователя с id={}", user.getId());
        if (!users.containsKey(user.getId())) {
            log.warn("Попытка обновить несуществующего пользователя с id={}", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("Пользователь с id={} обновлен в хранилище", user.getId());
        return user;
    }

    @Override
    public Collection<User> findAll() {
        log.debug("InMemoryUserStorage.findAll: возвращаем {} пользователей", users.size());
        return users.values();
    }

    @Override
    public User findById(int id) {
        log.debug("InMemoryUserStorage.findById: поиск пользователя с id={}", id);
        User user = users.get(id);
        if (user == null) {
            log.warn("Пользователь с id={} не найден в хранилище", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return user;
    }

    @Override
    public Collection<User> findByIds(Collection<Integer> ids) {
        log.debug("InMemoryUserStorage.findByIds: поиск {} пользователей по ID", ids.size());
        return ids.stream()
                .map(users::get)
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }

    @Override
    public void addFriend(int userId, int friendId) {

    }

    @Override
    public void removeFriend(int userId, int friendId) {

    }

    @Override
    public void approveFriend(int userId, int friendId) {

    }

    @Override
    public Collection<User> getFriends(int userId) {
        return List.of();
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        return List.of();
    }
}