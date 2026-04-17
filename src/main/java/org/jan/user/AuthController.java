package org.jan.user;

import jakarta.servlet.http.HttpSession;
import org.jan.user.CaptchaService.CaptchaChallenge;
import org.jan.user.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserService    userService;
    @Autowired private UserRepository userRepository;
    @Autowired private CaptchaService captchaService;

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
