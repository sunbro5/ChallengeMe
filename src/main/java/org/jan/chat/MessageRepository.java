package org.jan.chat;

import org.jan.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.sender = ?1 AND m.receiver = ?2) OR (m.sender = ?2 AND m.receiver = ?1) ORDER BY m.sentAt ASC")
    List<Message> findConversation(User user1, User user2);

    @Query("SELECT m FROM Message m WHERE m.sender = ?1 OR m.receiver = ?1")
    List<Message> findBySenderOrReceiver(User user);
}
