package com.mushroom.server.repository;

import com.mushroom.server.model.MushroomReference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MushroomRepository extends JpaRepository<MushroomReference, Integer> {
    Optional<MushroomReference> findByName(String name);
}