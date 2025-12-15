package com.mushroom.server.repository;

import com.mushroom.server.model.Message;
import com.mushroom.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    // Найти ВСЕ сообщения, где пользователь является или отправителем, или получателем.
    // Нужно для составления списка контактов.
    @Query("SELECT m FROM Message m WHERE m.sender = :user OR m.receiver = :user ORDER BY m.timestamp DESC")
    List<Message> findByUser(@Param("user") User user);

    // Найти переписку конкретно между двумя людьми (в обе стороны)
    // и отсортировать по времени (старые сверху, новые снизу)
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :u1 AND m.receiver = :u2) OR " +
            "(m.sender = :u2 AND m.receiver = :u1) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findHistory(@Param("u1") User u1, @Param("u2") User u2);
}