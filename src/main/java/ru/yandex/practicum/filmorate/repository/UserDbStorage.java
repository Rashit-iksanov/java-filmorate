package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        log.debug("Создание пользователя в БД: login='{}'", user.getLogin());
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            if (user.getBirthday() != null) {
                ps.setDate(4, Date.valueOf(user.getBirthday()));
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            return ps;
        }, keyHolder);

        // Присваиваю сгенерированный ID обратно объекту
        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        log.info("Пользователь успешно создан в БД с id={}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        log.debug("Обновление пользователя в БД с id={}", user.getId());
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());

        if (rowsAffected == 0) {
            log.warn("Попытка обновить несуществующего пользователя с id={}", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        log.info("Пользователь с id={} успешно обновлён в БД", user.getId());
        return user;
    }

    @Override
    public Collection<User> findAll() {
        log.debug("Получение списка всех пользователей из БД");
        String sql = "SELECT id, email, login, name, birthday FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public User findById(int id) {
        log.debug("Поиск пользователя в БД по id={}", id);
        String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";

        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);
        if (users.isEmpty()) {
            log.warn("Пользователь с id={} не найден в БД", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return users.get(0);
    }

    @Override
    public Collection<User> findByIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        log.debug("Поиск пользователей в БД по списку id: {}", ids);

        // Формирую строку вида "?, ?, ?"
        String inSql = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, email, login, name, birthday FROM users WHERE id IN (" + inSql + ")";

        return jdbcTemplate.query(sql, this::mapRowToUser, ids.toArray());
    }

    @Override
    public void addFriend(int userId, int friendId) {
        log.debug("Операция с друзьями: user {} -> friend {}", userId, friendId);

        // 1. Проверяю, не добавлял ли уже friendId пользователя userId в друзья
        String checkSql = "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?";
        List<String> statuses = jdbcTemplate.queryForList(checkSql, String.class, friendId, userId);

        if (!statuses.isEmpty()) {
            // 2. Если да, то это взаимное добавление (подтверждение дружбы)!
            log.info("Подтверждение дружбы между {} и {}", userId, friendId);

            // Обновляю существующую заявку на 'confirmed'
            jdbcTemplate.update(
                    "UPDATE friendships SET status = 'confirmed' WHERE user_id = ? AND friend_id = ?",
                    friendId, userId
            );
            // Создаю запись для текущего пользователя, чтобы дружба отображалась у обоих
            jdbcTemplate.update(
                    "MERGE INTO friendships (user_id, friend_id, status) KEY (user_id, friend_id) VALUES (?, ?, 'confirmed')",
                    userId, friendId
            );
        } else {
            // 3. Это первая заявка, создаю её со статусом 'unconfirmed'
            log.info("Создание новой заявки в друзья от {} к {}", userId, friendId);
            jdbcTemplate.update(
                    "MERGE INTO friendships (user_id, friend_id, status) KEY (user_id, friend_id) VALUES (?, ?, 'unconfirmed')",
                    userId, friendId
            );
        }
    }

    @Override
    public void approveFriend(int userId, int friendId) {
        String sql = "UPDATE friendships SET status = 'confirmed' WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, friendId, userId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        jdbcTemplate.update(sql, userId, friendId, friendId, userId);
    }

    @Override
    public Collection<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM users u JOIN friendships f ON u.id = f.friend_id WHERE f.user_id = ? AND f.status = 'confirmed'";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id " +
                "JOIN friendships f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? AND f1.status = 'confirmed' AND f2.status = 'confirmed'";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }
}