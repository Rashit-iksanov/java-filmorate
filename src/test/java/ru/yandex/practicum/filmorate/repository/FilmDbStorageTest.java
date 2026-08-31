package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class}) // <-- ИМПОРТИРУЕМ ОБА НУЖНЫХ БИНА
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;


    private Film createValidFilm(String name, String desc) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(desc);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        Mpa mpa = new Mpa();
        mpa.setId(1); // G
        mpa.setName("G");
        film.setMpa(mpa);
        return film;
    }

    @Test
    void testCreateAndFindFilm() {
        Film film = createValidFilm("Тестовый фильм", "Описание");
        Film created = filmStorage.create(film);

        assertThat(created.getId()).isGreaterThan(0);

        Film found = filmStorage.findById(created.getId());
        assertThat(found.getName()).isEqualTo("Тестовый фильм");
        assertThat(found.getMpa().getId()).isEqualTo(1); // Из data.sql
    }

    @Test
    void testAddAndRemoveLike() {
        User user = userStorage.create(new User() {{
            setEmail("l@mail.ru");
            setLogin("liker");
            setName("Liker");
            setBirthday(LocalDate.now());
        }});
        Film film = filmStorage.create(createValidFilm("Film", "Desc"));

        filmStorage.addLike(film.getId(), user.getId());
        Film filmWithLike = filmStorage.findById(film.getId());
        assertThat(filmWithLike.getLikes()).contains(user.getId());

        filmStorage.removeLike(film.getId(), user.getId());
        Film filmWithoutLike = filmStorage.findById(film.getId());
        assertThat(filmWithoutLike.getLikes()).doesNotContain(user.getId());
    }

    @Test
    void testGetPopularFilms() {
        User u1 = userStorage.create(new User() {{
            setEmail("1@mail.ru");
            setLogin("u1");
            setName("U1");
            setBirthday(LocalDate.now());
        }});
        User u2 = userStorage.create(new User() {{
            setEmail("2@mail.ru");
            setLogin("u2");
            setName("U2");
            setBirthday(LocalDate.now());
        }});

        Film popular = filmStorage.create(createValidFilm("Популярный", "Много лайков"));
        Film unpopular = filmStorage.create(createValidFilm("Непопулярный", "Мало лайков"));

        filmStorage.addLike(popular.getId(), u1.getId());
        filmStorage.addLike(popular.getId(), u2.getId()); // 2 лайка
        filmStorage.addLike(unpopular.getId(), u1.getId()); // 1 лайк

        Collection<Film> top1 = filmStorage.getPopular(1);
        assertThat(top1).hasSize(1);
        assertThat(top1.iterator().next().getName()).isEqualTo("Популярный");
    }
}