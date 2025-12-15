package com.mushroom.server.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Кто отправил
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    // Кому отправил
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(nullable = false)
    private String text;

    private LocalDateTime timestamp = LocalDateTime.now();
}