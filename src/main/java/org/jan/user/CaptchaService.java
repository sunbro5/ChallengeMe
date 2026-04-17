package org.jan.user;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keyless server-side math CAPTCHA.
 *
 * Flow:
 *   1. Client calls GET /auth/captcha  →  receives { id, question }
 *   2. User answers the math question
 *   3. Client includes captchaId + captchaAnswer in the register request
 *   4. Server calls verify() — each id can only be used once and expires after 10 min
 */
@Service
public class CaptchaService {

    private static final long   TTL_SECONDS = 600;   // 10 minutes
    private static final Random RNG         = new Random();

    private record Entry(String answer, Instant expiresAt) {}

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    /** Generates a new challenge. Returns { id, question }. The answer is never sent. */
    public CaptchaChallenge generate() {
        purgeExpired();

        int a, b;
        String question;
        int answer;

        int op = RNG.nextInt(3);  // 0=add, 1=subtract, 2=multiply
        switch (op) {
            case 1 -> {
                a = RNG.nextInt(10) + 1;       // 1..10
                b = RNG.nextInt(a) + 1;        // 1..a  (keeps result ≥ 0)
                question = a + " − " + b;
                answer   = a - b;
            }
            case 2 -> {
                a = RNG.nextInt(9) + 2;        // 2..10
                b = RNG.nextInt(9) + 2;        // 2..10
                question = a + " × " + b;
                answer   = a * b;
            }
            default -> {
                a = RNG.nextInt(20) + 1;       // 1..20
                b = RNG.nextInt(20) + 1;
                question = a + " + " + b;
                answer   = a + b;
            }
        }

        String id = UUID.randomUUID().toString();
        store.put(id, new Entry(String.valueOf(answer), Instant.now().plusSeconds(TTL_SECONDS)));
        return new CaptchaChallenge(id, question + " = ?");
    }

    /**
     * Verifies the user's answer.
     * The entry is removed on the first call — each id is single-use.
     */
    public boolean verify(String id, String userAnswer) {
        if (id == null || userAnswer == null || userAnswer.isBlank()) {
            return false;
        }
        Entry entry = store.remove(id);
        if (entry == null) {
            return false;
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            return false;
        }
        return entry.answer().equals(userAnswer.trim());
    }

    /** Remove expired entries so the map doesn't grow unbounded. */
    private void purgeExpired() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
    }

    /** DTO returned to the client. */
    public record CaptchaChallenge(String id, String question) {}
}
