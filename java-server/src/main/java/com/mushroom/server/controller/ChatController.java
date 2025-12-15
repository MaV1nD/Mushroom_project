package com.mushroom.server.controller;

import com.mushroom.server.model.Message;
import com.mushroom.server.model.MessageDTO;
import com.mushroom.server.model.User;
import com.mushroom.server.repository.MessageRepository;
import com.mushroom.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageDTO dto) {
        User sender = userRepository.findByUsername(dto.getSenderName()).orElseThrow();
        User receiver = userRepository.findByUsername(dto.getReceiverName()).orElseThrow();

        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setText(dto.getText());
        msg.setTimestamp(LocalDateTime.now());

        messageRepository.save(msg);
        return ResponseEntity.ok("Sent");
    }

    @GetMapping("/partners")
    public List<String> getPartners(@RequestParam String myName) {
        User me = userRepository.findByUsername(myName).orElseThrow();
        // Упрощенная логика: возвращаем логины тех, с кем переписывались
        List<Message> all = messageRepository.findByUser(me);
        return all.stream()
                .map(m -> m.getSender().equals(me) ? m.getReceiver().getUsername() : m.getSender().getUsername())
                .distinct()
                .collect(Collectors.toList());
    }

    @GetMapping("/history")
    public List<MessageDTO> getHistory(@RequestParam String user1, @RequestParam String user2) {
        User u1 = userRepository.findByUsername(user1).orElseThrow();
        User u2 = userRepository.findByUsername(user2).orElseThrow();

        List<Message> messages = messageRepository.findHistory(u1, u2);

        return messages.stream().map(m -> {
            MessageDTO dto = new MessageDTO();
            dto.setSenderName(m.getSender().getUsername());
            dto.setReceiverName(m.getReceiver().getUsername());
            dto.setText(m.getText());
            dto.setTimestamp(m.getTimestamp());

            dto.setSenderFullName(m.getSender().getFullName());

            return dto;
        }).collect(Collectors.toList());
    }
}