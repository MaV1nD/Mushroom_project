package com.mushroom.server.controller;

import com.mushroom.server.model.User;
import com.mushroom.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // Получить профиль любого пользователя по username
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    // Важно: не возвращаем пароль!
                    user.setPassword(null);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Обновить СВОЙ профиль
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestParam String username, @RequestBody User updatedData) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Обновляем только разрешенные поля
        user.setFullName(updatedData.getFullName());
        user.setPhoneNumber(updatedData.getPhoneNumber());
        user.setLocation(updatedData.getLocation());

        userRepository.save(user);
        return ResponseEntity.ok("Профиль обновлен");
    }
}