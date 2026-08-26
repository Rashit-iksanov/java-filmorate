-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    login VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    birthday DATE
);

-- Таблица возрастных рейтингов MPA
CREATE TABLE IF NOT EXISTS mpa (
    id INT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Таблица жанров
CREATE TABLE IF NOT EXISTS genres (
    id INT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- Таблица фильмов
CREATE TABLE IF NOT EXISTS films (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    release_date DATE,
    duration INT,
    mpa_id INT REFERENCES mpa(id)
);

-- Связь многие-ко-многим для фильмов и жанров (1NF: нет массивов в столбцах)
CREATE TABLE IF NOT EXISTS film_genres (
    film_id INT REFERENCES films(id) ON DELETE CASCADE,
    genre_id INT REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

-- Таблица дружеских связей со статусом
CREATE TABLE IF NOT EXISTS friendships (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    friend_id INT REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL CHECK (status IN ('unconfirmed', 'confirmed')),
    PRIMARY KEY (user_id, friend_id)
);

-- Таблица лайков (для определения популярности)
CREATE TABLE IF NOT EXISTS likes (
    film_id INT REFERENCES films(id) ON DELETE CASCADE,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);