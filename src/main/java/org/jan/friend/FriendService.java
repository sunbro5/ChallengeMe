package org.jan.friend;

import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    public Friendship sendRequest(User requester, String addresseeUsername) {
        User addressee = userRepository.findByUsername(addresseeUsername);
        if (addressee == null) throw new IllegalArgumentException("User not found");
        if (addressee.getId().equals(requester.getId())) throw new IllegalArgumentException("Cannot add yourself");
        friendshipRepository.findBetweenUsers(requester, addressee).ifPresent(f -> {
            throw new IllegalArgumentException("Friendship already exists");
        });
        return friendshipRepository.save(new Friendship(requester, addressee));
    }

    public Friendship acceptRequest(User addressee, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!friendship.getAddressee().getId().equals(addressee.getId()))
            throw new IllegalArgumentException("Not authorized");
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return friendshipRepository.save(friendship);
    }

    public List<User> getFriends(User user) {
        return friendshipRepository.findAcceptedFriendships(user).stream()
                .map(f -> f.getRequester().getId().equals(user.getId()) ? f.getAddressee() : f.getRequester())
                .collect(Collectors.toList());
    }

    public List<Friendship> getPendingRequests(User user) {
        return friendshipRepository.findPendingRequests(user);
    }
}
