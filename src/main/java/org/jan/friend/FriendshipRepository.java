package org.jan.friend;

import org.jan.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE (f.requester = ?1 OR f.addressee = ?1) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(User user);

    @Query("SELECT f FROM Friendship f WHERE f.addressee = ?1 AND f.status = 'PENDING'")
    List<Friendship> findPendingRequests(User user);

    @Query("SELECT f FROM Friendship f WHERE (f.requester = ?1 AND f.addressee = ?2) OR (f.requester = ?2 AND f.addressee = ?1)")
    Optional<Friendship> findBetweenUsers(User user1, User user2);

    @Query("SELECT f FROM Friendship f WHERE f.requester = ?1 OR f.addressee = ?1")
    List<Friendship> findByRequesterOrAddressee(User user);
}
