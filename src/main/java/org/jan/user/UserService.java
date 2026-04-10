package org.jan.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(String username, String password, String email) {
        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException("Username cannot be empty");
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Invalid email");
        if (userRepository.findByUsername(username) != null)
            throw new IllegalArgumentException("Username already exists");
        if (userRepository.findByEmail(email) != null)
            throw new IllegalArgumentException("Email already exists");

        return userRepository.save(new User(username, passwordEncoder.encode(password), email));
    }

    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword()))
            return null;
        if (user.isBanned())
            throw new IllegalArgumentException("Your account has been banned");
        return user;
    }
}
