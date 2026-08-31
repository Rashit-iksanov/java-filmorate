package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

public interface MpaRepository {

    List<Mpa> findAll();

    Optional<Mpa> findById(int id);
}
