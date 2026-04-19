package org.jan.friend;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.user.User;

@Entity
@Table(name = "friendships", indexes = {
    @Index(name = "idx_friendship_requester", columnList = "requester_id"),
    @Index(name = "idx_friendship_addressee", columnList = "addressee_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @JoinColumn(name = "addressee_id")
    private User addressee;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    public Friendship(User requester, User addressee) {
        this.requester = requester;
        this.addressee = addressee;
        this.status = FriendshipStatus.PENDING;
    }
}
