package org.jan.tournament;

import org.jan.game.GameEvent;
import org.jan.game.GameEventService;
import org.jan.game.GameType;
import org.jan.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TournamentService {

    @Autowired private TournamentRepository            tournamentRepository;
    @Autowired private TournamentParticipantRepository participantRepository;
    @Autowired private TournamentMatchRepository       matchRepository;
    @Autowired private GameEventService                gameEventService;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public Tournament createTournament(User creator, String name, String gameType, int capacity) {
        if (capacity != 4 && capacity != 8) {
            throw new IllegalArgumentException("Capacity must be 4 or 8");
        }
        if (name == null || name.isBlank() || name.length() > 80) {
            throw new IllegalArgumentException("Tournament name must be 1–80 characters");
        }
        // Validate gameType
        try { GameType.valueOf(gameType); } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown game type: " + gameType);
        }

        Tournament t = new Tournament();
        t.setName(name.strip());
        t.setGameType(gameType);
        t.setCapacity(capacity);
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

    /**
     * Creator starts the tournament. Shuffles participants, assigns seeds,
     * creates first-round TournamentMatch records and corresponding GameEvents.
     * The creator's current geolocation is used for all matches (passed in).
     */
    @Transactional
    public Tournament start(User creator, Long tournamentId, double latitude, double longitude) {
        Tournament t = load(tournamentId);
        if (!t.getCreator().getId().equals(creator.getId())) {
            throw new IllegalArgumentException("Only the creator can start the tournament");
        }
        if (!"OPEN".equals(t.getStatus())) {
            throw new IllegalArgumentException("Tournament has already started");
        }
        int count = participantRepository.countByTournament(t);
        if (count < t.getCapacity()) {
            throw new IllegalArgumentException(
                    "Not enough participants (" + count + "/" + t.getCapacity() + ")");
        }

        // Shuffle and assign seeds
        List<TournamentParticipant> parts = participantRepository.findByTournamentOrderBySeedAsc(t);
        Collections.shuffle(parts);
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).setSeed(i + 1);
        }
        participantRepository.saveAll(parts);

        t.setStatus("IN_PROGRESS");
        tournamentRepository.save(t);

        // Create first-round matches: pairs (1v2), (3v4), (5v6), (7v8)
        List<User> players = parts.stream().map(TournamentParticipant::getUser).toList();
        createRoundMatches(t, 1, players, latitude, longitude);

        return t;
    }

    // ── Record match result ───────────────────────────────────────────────────

    @Transactional
    public TournamentMatch recordResult(User requestor, Long matchId, Long winnerUserId,
                                        double latitude, double longitude) {
        TournamentMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        Tournament t = match.getTournament();

        // Only creator can record bracket results
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

        // Mark loser as eliminated
        User loser = winner.getId().equals(match.getPlayer1().getId())
                ? match.getPlayer2() : match.getPlayer1();
        participantRepository.findByTournamentOrderBySeedAsc(t).stream()
                .filter(p -> p.getUser().getId().equals(loser.getId()))
                .findFirst()
                .ifPresent(p -> { p.setEliminated(true); participantRepository.save(p); });

        // Check if current round is complete
        int round = match.getRound();
        List<TournamentMatch> roundMatches = matchRepository.findByTournamentAndRound(t, round);
        boolean roundComplete = roundMatches.stream().allMatch(m -> m.getWinner() != null);

        if (roundComplete) {
            List<User> winners = roundMatches.stream()
                    .map(TournamentMatch::getWinner)
                    .toList();

            if (winners.size() == 1) {
                // Final was just decided
                t.setWinner(winners.get(0));
                t.setStatus("FINISHED");
                tournamentRepository.save(t);
            } else {
                // Advance winners to next round
                createRoundMatches(t, round + 1, winners, latitude, longitude);
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

        List<TournamentDto.ParticipantDto> pDtos = parts.stream().map(p ->
                new TournamentDto.ParticipantDto(
                        p.getUser().getId(),
                        p.getUser().getUsername(),
                        p.getSeed(),
                        p.isEliminated()
                )).toList();

        List<TournamentDto.MatchDto> mDtos = matches.stream().map(m ->
                new TournamentDto.MatchDto(
                        m.getId(), m.getRound(), m.getMatchIndex(),
                        m.getPlayer1() != null ? m.getPlayer1().getUsername() : null,
                        m.getPlayer2() != null ? m.getPlayer2().getUsername() : null,
                        m.getGameEvent() != null ? m.getGameEvent().getId() : null,
                        m.getWinner()   != null ? m.getWinner().getUsername() : null
                )).toList();

        return new TournamentDto(
                t.getId(), t.getName(), t.getGameType(), t.getCapacity(), t.getStatus(),
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
     * Creates matches for a round from the given ordered list of players.
     * Pairs them sequentially: (0,1), (2,3), (4,5), …
     * For each match, creates a corresponding GameEvent on the map.
     */
    private void createRoundMatches(Tournament t, int round, List<User> players,
                                    double latitude, double longitude) {
        List<TournamentMatch> created = new ArrayList<>();
        for (int i = 0; i < players.size(); i += 2) {
            User p1 = players.get(i);
            User p2 = players.get(i + 1);
            TournamentMatch match = new TournamentMatch(t, round, i / 2, p1, p2);

            // Create a corresponding GameEvent (challenge on the map)
            // scheduled 24h from now, with invitedUsername = p2 (so only p2 can accept)
            try {
                GameEvent ge = gameEventService.createEvent(
                        p1, latitude, longitude,
                        GameType.valueOf(t.getGameType()),
                        LocalDateTime.now().plusHours(24),
                        "Turnaj: " + t.getName() + " (kolo " + round + ")",
                        null,
                        p2.getUsername()
                );
                match.setGameEvent(ge);
            } catch (Exception ignored) {
                // If creating the event fails (e.g. daily limit), proceed without it
            }

            created.add(match);
        }
        matchRepository.saveAll(created);
    }
}
