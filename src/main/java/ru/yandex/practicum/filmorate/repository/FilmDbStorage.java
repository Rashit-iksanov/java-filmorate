package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        log.debug("Создание фильма в БД: name='{}'", film.getName());
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        // Сохраняю жанры, если они есть
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }

        log.info("Фильм успешно создан в БД с id={}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        log.debug("Обновление фильма в БД с id={}", film.getId());

        // 1. Проверяю, существует ли фильм
        findById(film.getId());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?," +
                " mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        // 2. Обновляю жанры: сначала удаляю старые, потом добавляю новые
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }

        log.info("Фильм с id={} успешно обновлён в БД", film.getId());
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        log.debug("Получение списка всех фильмов из БД");
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa m ON f.mpa_id = m.id";

        List<Film> films = jdbcTemplate.query(sql, this::mapRow);
        // Загружаю жанры и лайкм для каждого фильма
        films.forEach(this::enrichFilm);
        return films;
    }

    @Override
    public Film findById(int id) {
        log.debug("Поиск фильма в БД по id={}", id);
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?";

        List<Film> films = jdbcTemplate.query(sql, this::mapRow, id);
        if (films.isEmpty()) {
            log.warn("Фильм с id={} не найден в БД", id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }

        Film film = films.get(0);
        films.forEach(this::enrichFilm);
        return film;
    }

    @Override
    public void addLike(int filmId, int userId) {
        log.debug("Добавление лайка: пользователь {} ставит лайк фильму {}", userId, filmId);
        // Используем MERGE для H2, чтобы избежать ошибок уникальности при повторном лайке
        String sql = "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        log.debug("Удаление лайка: пользователь {} удаляет лайк у фильма {}", userId, filmId);
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public Collection<Film> getPopular(int count) {
        log.debug("Получение {} популярных фильмов из БД", count);
        // Группируем по всем неагрегированным полям, чтобы соответствовать стандартам SQL
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_id, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                "ORDER BY likes_count DESC, f.id ASC LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, this::mapRow, count);
        films.forEach(this::enrichFilm);
        return films;
    }

    // --- Вспомогательные методы ---

    private void saveGenres(int filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            jdbcTemplate.update(sql, filmId, genre.getId());
        }
    }

    private Set<Integer> getLikesForFilm(int filmId) {
        return new HashSet<>(jdbcTemplate.queryForList("SELECT user_id FROM likes WHERE film_id = ?",
                Integer.class, filmId));
    }

    private Set<Genre> getGenresForFilm(int filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId);

        return new HashSet<>(genres);
    }

    private void enrichFilm(Film film) {
        film.setGenres(getGenresForFilm(film.getId()));
        film.setLikes(getLikesForFilm(film.getId()));
    }

    private Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        film.setGenres(new HashSet<>());

        return film;
    }
}