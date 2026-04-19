package org.jan.user;

import org.jan.achievement.AchievementDto;
import org.jan.achievement.AchievementRepository;
import org.jan.friend.FriendService;
import org.jan.game.EventStatus;
import org.jan.game.GameEvent;
import org.jan.game.GameEventRepository;
import org.jan.user.dto.GameHistoryDto;
import org.jan.user.dto.GameTypeStatsDto;
import org.jan.user.dto.PlayerProfileDto;
import org.jan.user.dto.RatingPointDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired private UserRepository        userRepository;
    @Autowired private FriendService         friendService;
    @Autowired private GameEventRepository   gameEventRepository;
    @Autowired private AchievementRepository achievementRepository;
    @Autowired private RatingHistoryRepository ratingHistoryRepository;

    @GetMapping("/{username}")
    public ResponseEntity<PlayerProfileDto> getProfile(
            @PathVariable String username, Authentication auth) {

        User player = userRepository.findByUsername(username);
        if (player == null || "ADMIN".equals(player.getRole())) {
            return ResponseEntity.notFound().build();
        }

        User viewer = (auth != null) ? userRepository.findByUsername(auth.getName()) : null;

        String friendStatus = "NONE";
        if (viewer != null && !viewer.getId().equals(player.getId())) {
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

        // ── Per-game-type stats (derived from history, no extra DB query) ──────
        Map<String, int[]> statsMap = new LinkedHashMap<>(); // int[] = {wins, losses, draws, disputes}
        for (GameEvent e : history) {
            String type = e.getGameType().name();
            statsMap.putIfAbsent(type, new int[4]);
            int[] s = statsMap.get(type);
            if (e.getStatus() == EventStatus.DISPUTED) {
                s[3]++;
            } else if (e.getWinner() == null) {
                s[2]++;
            } else if (e.getWinner().getId().equals(player.getId())) {
                s[0]++;
            } else {
                s[1]++;
            }
        }
        List<GameTypeStatsDto> gameStats = statsMap.entrySet().stream()
                .map(entry -> {
                    int[] s = entry.getValue();
                    return new GameTypeStatsDto(entry.getKey(), s[0], s[1], s[2], s[3]);
                })
                .sorted(Comparator.comparingInt((GameTypeStatsDto g) ->
                        g.wins() + g.losses() + g.draws() + g.disputes()).reversed())
                .collect(Collectors.toList());

        List<AchievementDto> achievements = achievementRepository
                .findByUserOrderByAwardedAtAsc(player)
                .stream()
                .map(a -> new AchievementDto(a.getAchievementType().name(), a.getAwardedAt()))
                .collect(Collectors.toList());

        PlayerProfileDto profile = PlayerProfileDto.builder()
                .id(player.getId())
                .username(player.getUsername())
                .bio(player.getBio())
                .favoriteGameKey(player.getFavoriteGameKey())
                .wins(player.getWins())
                .losses(player.getLosses())
                .draws(player.getDraws())
                .disputes(player.getDisputes())
                .rating(player.getRating())
                .friendStatus(friendStatus)
                .isMe(viewer != null && viewer.getId().equals(player.getId()))
                .games(games)
                .gameStats(gameStats)
                .achievements(achievements)
                .build();

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{username}/rating-history")
    public ResponseEntity<List<RatingPointDto>> getRatingHistory(@PathVariable String username) {
        User player = userRepository.findByUsername(username);
        if (player == null) return ResponseEntity.notFound().build();

        List<RatingPointDto> history = ratingHistoryRepository
                .findByUserOrderByRecordedAtAsc(player, org.springframework.data.domain.PageRequest.of(0, 100))
                .stream()
                .map(r -> new RatingPointDto(r.getRecordedAt(), r.getRatingAfter()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }

    @PutMapping("/me/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName());
        String bio = body.getOrDefault("bio", "");
        if (bio.length() > 160) bio = bio.substring(0, 160);
        user.setBio(bio.isBlank() ? null : bio);
        String favGame = body.get("favoriteGameKey");
        user.setFavoriteGameKey((favGame == null || favGame.isBlank()) ? null : favGame);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    private String blankToDrawLabel(String r) {
        return (r == null || r.isBlank()) ? "DRAW" : r;
    }
}
