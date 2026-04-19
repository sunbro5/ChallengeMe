package org.jan.user;

import jakarta.servlet.http.HttpSession;
import org.jan.user.CaptchaService.CaptchaChallenge;
import org.jan.user.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserService             userService;
    @Autowired private UserRepository          userRepository;
    @Autowired private CaptchaService          captchaService;
    @Autowired private PasswordResetRepository resetRepository;
    @Autowired private EmailService            emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** Generates a new math challenge for the registration form. Public endpoint. */
    @GetMapping("/captcha")
    public ResponseEntity<CaptchaChallenge> getCaptcha() {
        return ResponseEntity.ok(captchaService.generate());
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest req) {
        if (!captchaService.verify(req.getCaptchaId(), req.getCaptchaAnswer())) {
            return ResponseEntity.badRequest().body("Wrong CAPTCHA answer — please try again");
        }
        try {
            userService.registerUser(req.getUsername(), req.getPassword(), req.getEmail(), req.getBirthYear());
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
        try {
            User user = userService.authenticateUser(req.getUsername(), req.getPassword());
            if (user != null) {
                session.setAttribute("user", user);
                return ResponseEntity.ok(new LoginResponse(user.getUsername(), user.getRole()));
            }
            return ResponseEntity.badRequest().body("Invalid credentials");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out");
    }

    /**
     * Step 1 — the user submits their email.
     * Always returns 200 OK (we don't reveal whether the email is registered).
     */
    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user != null) {
            // Delete any existing token for this user first
            resetRepository.deleteByUser(user);
            String token = UUID.randomUUID().toString();
            resetRepository.save(new PasswordResetToken(token, user));
            emailService.sendPasswordResetEmail(email, token);
        }
        return ResponseEntity.ok("If the email exists, a reset link has been sent.");
    }

    /**
     * Step 2 — the user submits their new password together with the token from the email.
     */
    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        String token    = body.getOrDefault("token",    "").trim();
        String password = body.getOrDefault("password", "").trim();

        if (token.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body("Token and password are required.");
        }

        PasswordResetToken prt = resetRepository.findByToken(token).orElse(null);
        if (prt == null || prt.isExpired()) {
            return ResponseEntity.badRequest().body("Invalid or expired reset link.");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        resetRepository.delete(prt);

        return ResponseEntity.ok("Password reset successfully.");
    }

    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        userService.deleteAccount(user);
        session.invalidate();
        return ResponseEntity.ok("Account deleted");
    }
}
