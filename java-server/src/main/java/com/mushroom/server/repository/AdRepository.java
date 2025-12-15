package com.mushroom.server.repository;

import com.mushroom.server.model.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Integer> {

    @Query("SELECT a FROM Ad a WHERE " +
            "(:catId IS NULL OR a.category.id = :catId) AND " +
            "(:pattern IS NULL OR a.title LIKE :pattern OR a.description LIKE :pattern) " +
            "ORDER BY a.createdAt DESC")
    List<Ad> searchAds(@Param("pattern") String pattern, @Param("catId") Integer catId);
}