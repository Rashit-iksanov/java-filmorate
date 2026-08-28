package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.MpaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaRepository mpaRepository;

    public List<Mpa> findAll() {
        return mpaRepository.findAll();
    }

    public Mpa findById(int id) {
        return mpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mpa rating with id=" + id + " not found"));
    }
}
