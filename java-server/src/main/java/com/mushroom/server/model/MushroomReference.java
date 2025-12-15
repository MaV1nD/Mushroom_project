package com.mushroom.server.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mushroom_reference")
@Data
public class MushroomReference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name; // Ключ (slippery_jack)
    private String displayName; // Русское название
    private Boolean isEdible; // Съедобен?

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String cookingTips;
}