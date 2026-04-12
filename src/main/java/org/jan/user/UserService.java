package org.jan.user;

import org.jan.chat.MessageRepository;
import org.jan.config.ProfanityFilter;
import org.jan.friend.FriendshipRepository;
import org.jan.game.GameEventRepository;
import org.jan.report.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired private UserRepository       userRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private MessageRepository    messageRepository;
    @Autowired private GameEventRepository  gameEventRepository;
    @Autowired private ReportRepository     reportRepository;
    @Autowired private ProfanityFilter      profanityFilter;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(String username, String password, String email, int birthYear) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        int currentYear = java.time.Year.now().getValue();
        if (birthYear <= 0 || birthYear > currentYear) {
            throw new IllegalArgumentException("Invalid birth year");
        }
        if (currentYear - birthYear < 18) {
            throw new IllegalArgumentException("You must be at least 18 years old to register");
        }
        profanityFilter.check(username);
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User(username, passwordEncoder.encode(password), email);
        user.setBirthYear(birthYear);
        return userRepository.save(user);
    }

    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        if (user.isBanned()) {
            throw new IllegalArgumentException("Your account has been banned");
        }
        return user;
    }

    /**
     * Permanently deletes all data belonging to the user (GDPR right to erasure).
     * Order matters: clear FK references before removing the user row.
     */
    @Transactional
    public void deleteAccount(User user) {
        // 1. Clear winner references so no event points to this user
        gameEventRepository.findByWinner(user)
                .forEach(e -> { e.setWinner(null); gameEventRepository.save(e); });

        // 2. Remove user from all participant lists (events they joined but didn't create)
        gameEventRepository.findByParticipantsContaining(user)
                .forEach(e -> { e.getParticipants().remove(user); gameEventRepository.save(e); });

        // 3. Delete all events this user created (participants already cleaned above)
        gameEventRepository.deleteAll(gameEventRepository.findByCreator(user));

        // 4. Delete friendships, messages, reports
        friendshipRepository.deleteAll(friendshipRepository.findByRequesterOrAddressee(user));
        messageRepository.deleteAll(messageRepository.findBySenderOrReceiver(user));
        reportRepository.deleteAll(reportRepository.findByReporterOrReported(user, user));

        // 5. Delete the user
        userRepository.delete(user);
    }
}
