package com.mushroom.server.controller;

import com.mushroom.server.model.User;
import com.mushroom.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Говорит Spring'у, что этот класс обрабатывает HTTP запросы
@RequestMapping("/api/auth") // Все методы будут начинаться с этого адреса
@RequiredArgsConstructor // Создает конструктор для userRepository (Lombok)
public class AuthController {

    private final UserRepository userRepository;

    // Метод регистрации
    // POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        // 1. Проверяем, занят ли логин
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Ошибка: Пользователь с таким логином уже существует!");
        }

        // 2. Сохраняем в базу
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok("Пользователь " + savedUser.getUsername() + " успешно зарегистрирован!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        // 1. Ищем пользователя
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // 2. Проверяем пароль
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.badRequest().body("Неверный пароль!");
        }

        // 3. Если всё ок — возвращаем успешный ответ
        return ResponseEntity.ok("Успешный вход");
    }

}