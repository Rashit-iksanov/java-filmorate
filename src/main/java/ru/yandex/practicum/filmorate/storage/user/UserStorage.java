package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User create(User user);

    User update(User user);

    Collection<User> findAll();

    User findById(int id);

    Collection<User> findByIds(Collection<Integer> ids);

    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    void approveFriend(int userId, int friendId); // userId подтверждает заявку от friendId

    Collection<User> getFriends(int userId);

    Collection<User> getCommonFriends(int userId, int otherId);
}