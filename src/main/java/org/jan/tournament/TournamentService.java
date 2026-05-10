package org.jan.tournament;

import org.jan.game.GameEvent;
import org.jan.game.GameEventService;
import org.jan.game.GameType;
import org.jan.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TournamentService {

    @Autowired private TournamentRepository            tournamentRepository;
    @Autowired private TournamentParticipantRepository participantRepository;
    @Autowired private TournamentMatchRepository       matchRepository;
    @Autowired private GameEventService                gameEventService;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public Tournament createTournament(User creator, String name, String gameType,
                                       int capacity, String format) {
        if (format == null || (!format.equals("ELIMINATION") && !format.equals("ROUND_ROBIN"))) {
            throw new IllegalArgumentException("Format must be ELIMINATION or ROUND_ROBIN");
        }
        if ("ELIMINATION".equals(format)) {
            if (capacity != 2 && capacity != 4 && capacity != 8 && capacity != 16) {
                throw new IllegalArgumentException("Elimination capacity must be 2, 4, 8 or 16");
            }
        } else {
            if (capacity < 3 || capacity > 32) {
                throw new IllegalArgumentException("Round-robin capacity must be 3–32");
            }
        }
        if (name == null || name.isBlank() || name.length() > 80) {
            throw new IllegalArgumentException("Tournament name must be 1–80 characters");
        }
        try { GameType.valueOf(gameType); } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown game type: " + gameType);
        }

        Tournament t = new Tournament();
        t.setName(name.strip());
        t.setGameType(gameType);
        t.setCapacity(capacity);
        t.setFormatType(format);
        t.setCreator(creator);
        t.setStatus("OPEN");
        Tournament saved = tournamentRepository.save(t);

        // Creator automatically joins
        participantRepository.save(new TournamentParticipant(saved, creator));
        return saved;
    }

    // ── Join ─────────────────────────────────────────────────────────────────

    @Transactional
    public Tournament join(User user, Long tournamentId) {
        Tournament t = load(tournamentId);
        if (!"OPEN".equals(t.getStatus())) {
            throw new IllegalArgumentException("Tournament is not open for sign-ups");
        }
        if (participantRepository.existsByTournamentAndUser(t, user)) {
            throw new IllegalArgumentException("You are already registered");
        }
        int count = participantRepository.countByTournament(t);
        if (count >= t.getCapacity()) {
            throw new IllegalArgumentException("Tournament is full");
        }
        participantRepository.save(new TournamentParticipant(t, user));
        return t;
    }

    // ── Start ─────────────────────────────────────────────────────────────────

    @Transactional
    public Tournament start(User creator, Long tournamentId, double latitude, double longitude) {
        Tournament t = load(tournamentId);
        if (!t.getCreator().getId().equals(creator.getId())) {
            throw new IllegalArgumentException("Only the creator can start the tournament");
        }
        if (!"OPEN".equals(t.getStatus())) {
            throw new IllegalArgumentException("Tournament has already started");
        }

        List<TournamentParticipant> parts = participantRepository.findByTournamentOrderBySeedAsc(t);
        int count = parts.size();

        if ("ELIMINATION".equals(t.getFormatType())) {
            if (count < t.getCapacity()) {
                throw new IllegalArgumentException(
                        "Not enough participants (" + count + "/" + t.getCapacity() + ")");
            }
        } else {
            // ROUND_ROBIN: need at least 3
            if (count < 3) {
                throw new IllegalArgumentException("At least 3 players are required to start");
            }
        }

        // Shuffle and assign seeds
        Collections.shuffle(parts);
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).setSeed(i + 1);
        }
        participantRepository.saveAll(parts);

        t.setStatus("IN_PROGRESS");
        t.setLatitude(latitude);
        t.setLongitude(longitude);
        tournamentRepository.save(t);

        List<User> players = parts.stream().map(TournamentParticipant::getUser).toList();

        if ("ELIMINATION".equals(t.getFormatType())) {
            createRoundMatches(t, 1, players, latitude, longitude);
        } else {
            createRoundRobinMatches(t, players, latitude, longitude);
        }

        return t;
    }

    // ── Record match result ───────────────────────────────────────────────────

    @Transactional
    public TournamentMatch recordResult(User requestor, Long matchId, Long winnerUserId) {
        TournamentMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        Tournament t = match.getTournament();

        if (!t.getCreator().getId().equals(requestor.getId())) {
            throw new IllegalArgumentException("Only the creator can record match results");
        }
        if (match.getWinner() != null) {
            throw new IllegalArgumentException("Match already has a result");
        }

        // Validate winner is one of the match players
        User winner;
        if (match.getPlayer1().getId().equals(winnerUserId)) {
            winner = match.getPlayer1();
        } else if (match.getPlayer2().getId().equals(winnerUserId)) {
            winner = match.getPlayer2();
        } else {
            throw new IllegalArgumentException("Winner must be one of the match players");
        }
        match.setWinner(winner);
        matchRepository.save(match);

        if ("ROUND_ROBIN".equals(t.getFormatType())) {
            handleRoundRobinComplete(t);
        } else {
            // ELIMINATION: mark loser as eliminated, advance bracket when round is done
            User loser = winner.getId().equals(match.getPlayer1().getId())
                    ? match.getPlayer2() : match.getPlayer1();
            participantRepository.findByTournamentOrderBySeedAsc(t).stream()
                    .filter(p -> p.getUser().getId().equals(loser.getId()))
                    .findFirst()
                    .ifPresent(p -> { p.setEliminated(true); participantRepository.save(p); });

            int round = match.getRound();
            List<TournamentMatch> roundMatches = matchRepository.findByTournamentAndRound(t, round);
            boolean roundComplete = roundMatches.stream().allMatch(m -> m.getWinner() != null);

            if (roundComplete) {
                List<User> winners = roundMatches.stream()
                        .map(TournamentMatch::getWinner)
                        .toList();
                if (winners.size() == 1) {
                    t.setWinner(winners.get(0));
                    t.setStatus("FINISHED");
                    tournamentRepository.save(t);
                } else {
                    createRoundMatches(t, round + 1, winners, t.getLatitude(), t.getLongitude());
                }
            }
        }

        return match;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Tournament> getActive() {
        return tournamentRepository.findActive();
    }

    @Transactional(readOnly = true)
    public TournamentDto getDetail(Long id) {
        Tournament t = load(id);
        List<TournamentParticipant> parts = participantRepository.findByTournamentOrderBySeedAsc(t);
        List<TournamentMatch> matches = matchRepository.findByTournamentOrderByRoundAscMatchIndexAsc(t);

        // For ROUND_ROBIN: compute wins/losses per participant from match results
        Map<Long, int[]> stats = new HashMap<>(); // userId → [wins, losses]
        if ("ROUND_ROBIN".equals(t.getFormatType())) {
            for (TournamentParticipant p : parts) {
                stats.put(p.getUser().getId(), new int[]{0, 0});
            }
            for (TournamentMatch m : matches) {
                if (m.getWinner() != null) {
                    stats.computeIfPresent(m.getWinner().getId(),
                            (k, v) -> { v[0]++; return v; });
                    User loser = m.getWinner().getId().equals(m.getPlayer1().getId())
                            ? m.getPlayer2() : m.getPlayer1();
                    stats.computeIfPresent(loser.getId(),
                            (k, v) -> { v[1]++; return v; });
                }
            }
        }

        List<TournamentDto.ParticipantDto> pDtos = parts.stream().map(p -> {
            int[] s = stats.getOrDefault(p.getUser().getId(), new int[]{0, 0});
            return new TournamentDto.ParticipantDto(
                    p.getUser().getId(),
                    p.getUser().getUsername(),
                    p.getSeed(),
                    p.isEliminated(),
                    s[0],
                    s[1]
            );
        }).toList();

        List<TournamentDto.MatchDto> mDtos = matches.stream().map(m ->
                new TournamentDto.MatchDto(
                        m.getId(), m.getRound(), m.getMatchIndex(),
                        m.getPlayer1() != null ? m.getPlayer1().getUsername() : null,
                        m.getPlayer2() != null ? m.getPlayer2().getUsername() : null,
                        m.getGameEvent() != null ? m.getGameEvent().getId() : null,
                        m.getWinner()   != null ? m.getWinner().getUsername() : null
                )).toList();

        return new TournamentDto(
                t.getId(), t.getName(), t.getGameType(), t.getCapacity(),
                t.getFormatType(),
                t.getStatus(),
                t.getCreator().getUsername(),
                t.getWinner() != null ? t.getWinner().getUsername() : null,
                t.getCreatedAt(),
                parts.size(),
                pDtos, mDtos
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Tournament load(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
    }

    /**
     * Creates elimination-bracket matches for a round.
     * Pairs players sequentially: (0,1), (2,3), …
     * Creates a corresponding GameEvent (map challenge) for each match.
     */
    private void createRoundMatches(Tournament t, int round, List<User> players,
                                    double latitude, double longitude) {
        List<TournamentMatch> created = new ArrayList<>();
        for (int i = 0; i < players.size(); i += 2) {
            User p1 = players.get(i);
            User p2 = players.get(i + 1);
            TournamentMatch match = new TournamentMatch(t, round, i / 2, p1, p2);
            try {
                GameEvent ge = gameEventService.createEvent(
                        p1, latitude, longitude,
                        GameType.valueOf(t.getGameType()),
                        LocalDateTime.now().plusHours(24),
                        "Turnaj: " + t.getName() + " (kolo " + round + ")",
                        null,
                        p2.getUsername(),
                        false,
                        "TOURNAMENT"
                );
                match.setGameEvent(ge);
            } catch (Exception ignored) { /* proceed without map event if creation fails */ }
            created.add(match);
        }
        matchRepository.saveAll(created);
    }

    /**
     * Creates round-robin schedule using the circle method.
     * Creates a GameEvent (map challenge) for each match so it appears on the map.
     * Handles odd number of players by adding a bye (null) participant.
     */
    private void createRoundRobinMatches(Tournament t, List<User> players,
                                          double latitude, double longitude) {
        List<User> circle = new ArrayList<>(players);
        int n = circle.size();
        if (n % 2 != 0) { circle.add(null); n++; } // null = bye
        int totalRounds = n - 1;
        List<TournamentMatch> allMatches = new ArrayList<>();
        for (int round = 1; round <= totalRounds; round++) {
            int matchIdx = 0;
            for (int i = 0; i < n / 2; i++) {
                User home = circle.get(i);
                User away = circle.get(n - 1 - i);
                if (home != null && away != null) { // skip bye
                    TournamentMatch match = new TournamentMatch(t, round, matchIdx++, home, away);
                    try {
                        GameEvent ge = gameEventService.createEvent(
                                home, latitude, longitude,
                                GameType.valueOf(t.getGameType()),
                                LocalDateTime.now().plusHours(24),
                                "Turnaj: " + t.getName() + " (kolo " + round + ")",
                                null, away.getUsername(), false, "TOURNAMENT");
                        match.setGameEvent(ge);
                    } catch (Exception ignored) { /* proceed without map event if creation fails */ }
                    allMatches.add(match);
                }
            }
            // Rotate: keep position 0 fixed, move last element to position 1
            User last = circle.remove(n - 1);
            circle.add(1, last);
        }
        matchRepository.saveAll(allMatches);
    }

    /**
     * Called after every round-robin result is recorded.
     * If all matches are finished, determines the winner and closes the tournament.
     */
    private void handleRoundRobinComplete(Tournament t) {
        List<TournamentMatch> allMatches =
                matchRepository.findByTournamentOrderByRoundAscMatchIndexAsc(t);
        boolean allDone = allMatches.stream().allMatch(m -> m.getWinner() != null);
        if (!allDone) return;

        // Tally wins and losses
        List<TournamentParticipant> parts = participantRepository.findByTournamentOrderBySeedAsc(t);
        Map<Long, Integer> wins   = new HashMap<>();
        Map<Long, Integer> losses = new HashMap<>();
        for (TournamentParticipant p : parts) {
            wins.put(p.getUser().getId(), 0);
            losses.put(p.getUser().getId(), 0);
        }
        for (TournamentMatch m : allMatches) {
            if (m.getWinner() != null) {
                wins.merge(m.getWinner().getId(), 1, Integer::sum);
                User loser = m.getWinner().getId().equals(m.getPlayer1().getId())
                        ? m.getPlayer2() : m.getPlayer1();
                losses.merge(loser.getId(), 1, Integer::sum);
            }
        }

        // Best participant: most wins, fewest losses as tiebreaker
        parts.sort((a, b) -> {
            int wDiff = wins.getOrDefault(b.getUser().getId(), 0)
                      - wins.getOrDefault(a.getUser().getId(), 0);
            if (wDiff != 0) return wDiff;
            return losses.getOrDefault(a.getUser().getId(), 0)
                 - losses.getOrDefault(b.getUser().getId(), 0);
        });

        t.setWinner(parts.get(0).getUser());
        t.setStatus("FINISHED");
        tournamentRepository.save(t);
    }
}
