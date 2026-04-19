package org.jan.notification;

import org.jan.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    long countByUserAndReadFalse(User user);

    @Modifying @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user")
    void markAllRead(@Param("user") User user);

    @Modifying @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id AND n.user = :user")
    void markRead(@Param("id") Long id, @Param("user") User user);
}
