package com.mushroom.server.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private String senderName;
    private String receiverName;
    private String text;
    private LocalDateTime timestamp;

    // НОВОЕ ПОЛЕ
    private String senderFullName;
}