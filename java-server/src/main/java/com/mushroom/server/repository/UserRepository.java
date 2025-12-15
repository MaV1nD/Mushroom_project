package com.mushroom.server.repository;

import com.mushroom.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<User, Integer> означает:
// Работаем с сущностью User, у которой ID имеет тип Integer
public interface UserRepository extends JpaRepository<User, Integer> {

    // Найти пользователя по имени
    Optional<User> findByUsername(String username);

    // Проверить, существует ли такой логин
    boolean existsByUsername(String username);
}