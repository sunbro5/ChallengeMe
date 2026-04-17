package org.jan.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsername(adminUsername) == null) {
            User admin = new User(adminUsername, passwordEncoder.encode(adminPassword), adminUsername + "@admin.local", "ADMIN");
            User test = new User("test", passwordEncoder.encode("test"), "test" + "@admin.local", "USER");
            userRepository.saveAll(List.of(admin, test));
        }
    }
}
