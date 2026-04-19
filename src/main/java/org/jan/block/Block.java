package org.jan.block;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocks",
       uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"}),
       indexes = @Index(name = "idx_block_blocker", columnList = "blocker_id"))
@Getter @Setter @NoArgsConstructor
public class Block {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Block(User blocker, User blocked) {
        this.blocker   = blocker;
        this.blocked   = blocked;
        this.createdAt = LocalDateTime.now();
    }
}
