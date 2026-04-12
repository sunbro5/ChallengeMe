package org.jan.user;

import org.jan.friend.FriendService;
import org.jan.game.EventStatus;
import org.jan.game.GameEvent;
import org.jan.game.GameEventRepository;
import org.jan.user.dto.GameHistoryDto;
import org.jan.user.dto.PlayerProfileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/players")
public class PlayerController {

    @Autowired private UserRepository      userRepository;
    @Autowired private FriendService       friendService;
    @Autowired private GameEventRepository gameEventRepository;

    @GetMapping("/{username}")
    public ResponseEntity<PlayerProfileDto> getProfile(
            @PathVariable String username, Authentication auth) {

        User player = userRepository.findByUsername(username);
        if (player == null || "ADMIN".equals(player.getRole())) {
            return ResponseEntity.notFound().build();
        }

        User viewer = userRepository.findByUsername(auth.getName());

        String friendStatus = "NONE";
        if (!viewer.getId().equals(player.getId())) {
            friendStatus = friendService.getFriendshipStatus(viewer, player);
        }

        List<GameEvent> history = gameEventRepository.findByParticipantAndStatusIn(
                player, List.of(EventStatus.FINISHED, EventStatus.DISPUTED));

        List<GameHistoryDto> games = history.stream().map(e -> {
            String opponentUsername = e.getParticipants().stream()
                    .filter(p -> !p.getId().equals(player.getId()))
                    .findFirst()
                    .map(User::getUsername)
                    .orElse("—");

            String result;
            if (e.getStatus() == EventStatus.DISPUTED) {
                result = "disputed";
            } else if (e.getWinner() == null) {
                result = "draw";
            } else if (e.getWinner().getId().equals(player.getId())) {
                result = "won";
            } else {
                result = "lost";
            }

            GameHistoryDto.GameHistoryDtoBuilder b = GameHistoryDto.builder()
                    .id(e.getId())
                    .gameType(e.getGameType().name())
                    .scheduledAt(e.getScheduledAt())
                    .status(e.getStatus().name())
                    .opponentUsername(opponentUsername)
                    .result(result)
                    .resultNote(e.getResultNote());

            if (e.getStatus() == EventStatus.DISPUTED) {
                b.creatorUsername(e.getCreator().getUsername())
                 .creatorResult(blankToDrawLabel(e.getCreatorResult()))
                 .challengerResult(blankToDrawLabel(e.getChallengerResult()));
            }

            return b.build();
        }).collect(Collectors.toList());

        PlayerProfileDto profile = PlayerProfileDto.builder()
                .id(player.getId())
                .username(player.getUsername())
                .wins(player.getWins())
                .losses(player.getLosses())
                .draws(player.getDraws())
                .disputes(player.getDisputes())
                .friendStatus(friendStatus)
                .isMe(viewer.getId().equals(player.getId()))
                .games(games)
                .build();

        return ResponseEntity.ok(profile);
    }

    private String blankToDrawLabel(String r) {
        return (r == null || r.isBlank()) ? "DRAW" : r;
    }
}
