package org.jan.game;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_events", indexes = {
    @Index(name = "idx_event_status_created",     columnList = "status, created_at DESC"),
    @Index(name = "idx_event_creator",            columnList = "creator_id"),
    @Index(name = "idx_event_status_scheduledat", columnList = "status, scheduled_at")
})
@Getter
@Setter
@NoArgsConstructor
public class GameEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id")
    private User creator;

    private double latitude;
    private double longitude;

    @Enumerated(EnumType.STRING)
    private GameType gameType;

    private LocalDateTime scheduledAt;

    /** Optional free-text description from the creator (e.g. skill level, location details). */
    @Column(length = 500)
    private String description;

    /** Optional human-readable venue name, e.g. "Table in Stromovka park". */
    @Column(length = 80)
    private String locationName;

    /** If set, only this username can accept the challenge (direct invite). */
    @Column(length = 50)
    private String invitedUsername;

    /** Wall-clock time when the event was posted. Used for the daily creation limit. */
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.OPEN;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "game_event_participants",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner_id", nullable = true)
    private User winner;

    private String resultNote;

    /**
     * What the creator reported as winner username, or "" for draw.
     * Null means the creator has not yet submitted.
     */
    private String creatorResult;

    /**
     * What the challenger reported as winner username, or "" for draw.
     * Null means the challenger has not yet submitted.
     */
    private String challengerResult;

    private String creatorResultNote;
    private String challengerResultNote;

    // ── Computed helpers ──────────────────────────────────────────────────────

    public boolean isCreatorResultSubmitted()    { return creatorResult != null; }
    public boolean isChallengerResultSubmitted() { return challengerResult != null; }

    /** Returns the non-creator participant, or null if not yet accepted. */
    public User getChallenger() {
        return participants.stream()
                .filter(p -> !p.getId().equals(creator.getId()))
                .findFirst()
                .orElse(null);
    }
}
