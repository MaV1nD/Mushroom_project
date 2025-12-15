package org.example.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private String senderName;
    private String receiverName;
    private String text;
    private LocalDateTime timestamp;
    private String senderFullName;
}