package org.jan.notification;

import org.jan.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void create(User user, String type, String actorUsername, String text, Long eventId) {
        notificationRepository.save(new Notification(user, type, actorUsername, text, eventId));
    }

    @Transactional(readOnly = true)
    public List<Notification> getForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public long countUnread(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void markAllRead(User user) {
        notificationRepository.markAllRead(user);
    }

    @Transactional
    public void markRead(Long id, User user) {
        notificationRepository.markRead(id, user);
    }
}
