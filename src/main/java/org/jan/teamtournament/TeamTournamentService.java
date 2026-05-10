package org.jan.teamtournament;

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
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamTournamentService {

    @Autowired private TeamTournamentRepository       tournamentRepository;
    @Autowired private TournamentTeamRepository       teamRepository;
    @Autowired private TournamentTeamMemberRepository memberRepository;
    @Autowired private TeamTournamentMatchRepository  matchRepository;
    @Autowired private GameEventService               gameEventService;

    // ── Create tournament ─────────────────────────────────────────────────────

    @Transactional
    public TeamTournament createTournament(User creator, String name, String gameType,
                                           int teamCapacity, int teamSizeMax, String format) {
        if (name == null || name.isBlank() || name.length() > 80) {
            throw new IllegalArgumentException("Tournament name must be 1–80 characters");
        }
        if (!"ELIMINATION".equals(format) && !"ROUND_ROBIN".equals(format)) {
            throw new IllegalArgumentException("Format must be ELIMINATION or ROUND_ROBIN");
        }
        if (teamCapacity < 2 || teamCapacity > 32) {
            throw new IllegalArgumentException("Team capacity must be 2–32");
        }
        if ("ELIMINATION".equals(format)) {
            if (teamCapacity != 2 && teamCapacity != 4 && teamCapacity != 8 && teamCapacity != 16) {
                throw new IllegalArgumentException(
                        "ELIMINATION format requires team capacity of 2, 4, 8, or 16");
            }
        }
        if (teamSizeMax < 2 || teamSizeMax > 20) {
            throw new IllegalArgumentException("Team size max must be 2–20");
        }

        TeamTournament t = new TeamTournament();
        t.setName(name.strip());
        t.setGameType(gameType);
        t.setTeamCapacity(teamCapacity);
        t.setTeamSizeMax(teamSizeMax);
        t.setFormat(format);
        t.setStatus("OPEN");
        t.setCreator(creator);
        t.setCreatedAt(LocalDateTime.now());
        return tournamentRepository.save(t);
    }

    // ── Create team ───────────────────────────────────────────────────────────

    @Transactional
    public TournamentTeam createTeam(User captain, Long tournamentId, String teamName) {
        TeamTournament t = loadTournament(tournamentId);
        if (!"OPEN".equals(t.getStatus())) {
            throw new IllegalArgumentException("Tournament is not open for registration");
        }
        if (teamName == null || teamName.isBlank() || teamName.length() > 60) {
            throw new IllegalArgumentException("Team name must be 1–60 characters");
        }
        if (teamRepository.existsByTournamentAndName(t, teamName.strip())) {
            throw new IllegalArgumentException("A team with that name already exists in this tournament");
        }
        int currentTeams = teamRepository.countByTournament(t);
        if (currentTeams >= t.getTeamCapacity()) {
            throw new IllegalArgumentException("Tournament is full — no more teams can register");
        }
        // Captain may not already be in another team in this tournament
        if (memberRepository.existsByTournamentAndUser(t, captain)) {
            throw new IllegalArgumentException("You are already in a team in this tournament");
        }

        TournamentTeam team = teamRepository.save(new TournamentTeam(t, teamName.strip(), captain));
        // Captain automatically becomes the first member
        memberRepository.save(new TournamentTeamMember(team, captain));
        return team;
    }

    // ── Join team ─────────────────────────────────────────────────────────────

    @Transactional
    public void joinTeam(User user, Long teamId) {
        TournamentTeam team = loadTeam(teamId);
        TeamTournament t = team.getTournament();
        if (!"OPEN".equals(t.getStatus())) {
            throw new IllegalArgumentException("Tournament is not open for registration");
        }
        if (memberRepository.existsByTournamentAndUser(t, user)) {
            throw new IllegalArgumentException("You are already in a team in this tournament");
        }
        int currentMembers = memberRepository.countByTeam(team);
        if (currentMembers >= t.getTeamSizeMax()) {
            throw new IllegalArgumentException("Team is full");
        }
        memberRepository.save(new TournamentTeamMember(team, user));
    }

    // ── Leave team ────────────────────────────────────────────────────────────

    @Transactional
    public void leaveTeam(User user, Long teamId) {
        TournamentTeam team = loadTeam(teamId);
        TeamTournament t = team.getTournament();
        if (!"OPEN".equals(t.getStatus())) {
            throw new IllegalArgumentException("Cannot leave a team once the tournament has started");
        }
        if (team.getCaptain().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "Captain cannot leave — disband the team instead");
        }
        TournamentTeamMember member = memberRepository.findByTeam(team).stream()
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this team"));
        memberRepository.delete(member);
    }

    // ── Kick member (captain only) ────────────────────────────────────────────

    @Transactional
    public void kickMember(User captain, Long teamId, Long targetUserId) {
        TournamentTeam team = loadTeam(teamId);
        TeamTournament t = team.getTournament();
        if (!"OPEN".equals(t.getStatus())) {
            throw new IllegalArgumentException("Cannot remove members after the tournament has started");
        }
        if (!team.getCaptain().getId().equals(captain.getId())) {
            throw new IllegalArgumentException("Only the team captain can remove members");
        }
        if (captain.getId().equals(targetUserId)) {
            throw new IllegalArgumentException("Captain cannot remove themselves");
        }
        TournamentTeamMember member = memberRepository.findByTeam(team).stream()
                .filter(m -> m.getUser().getId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this team"));
        memberRepository.delete(member);
    }

    // ── Start tournament ──────────────────────────────────────────────────────

    @Transactional
    public TeamTournament start(User creator, Long tournamentId, double latitude, double longitude) {
        TeamTournament t = loadTournament(tournamentId);
        if (!t.getCreator().getId().equals(creator.getId())) {
            throw new IllegalArgumentException("Only the creator can start the tournament");
        }
        if (!"OPEN".equals(t.getStatus())) {
            throw new IllegalArgumentException("Tournament has already started or is finished");
        }

        List<TournamentTeam> teams = teamRepository.findByTournament(t);
        int count = teams.size();

        if ("ELIMINATION".equals(t.getFormat())) {
            if (count < t.getTeamCapacity()) {
                throw new IllegalArgumentException(
                        "All slots must be filled before starting an elimination tournament ("
                        + count + "/" + t.getTeamCapacity() + ")");
            }
        } else {
            // ROUND_ROBIN: minimum 2 teams
            if (count < 2) {
                throw new IllegalArgumentException(
                        "At least 2 teams are required to start the tournament");
            }
        }

        // Shuffle and assign seeds
        Collections.shuffle(teams);
        for (int i = 0; i < teams.size(); i++) {
            teams.get(i).setSeed(i + 1);
        }
        teamRepository.saveAll(teams);

        t.setStatus("IN_PROGRESS");
        t.setLatitude(latitude);
        t.setLongitude(longitude);
        tournamentRepository.save(t);

        if ("ELIMINATION".equals(t.getFormat())) {
            createEliminationRound(t, 1, teams, latitude, longitude);
        } else {
            createRoundRobinMatches(t, teams, latitude, longitude);
        }

        return t;
    }

    // ── Record match result ───────────────────────────────────────────────────

    @Transactional
    public TeamTournamentMatch recordResult(User requestor, Long matchId,
                                            String result, String note) {
        TeamTournamentMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        TeamTournament t = match.getTournament();

        if (!t.getCreator().getId().equals(requestor.getId())) {
            throw new IllegalArgumentException("Only the creator can record match results");
        }
        if ("FINISHED".equals(match.getStatus())) {
            throw new IllegalArgumentException("Match result has already been recorded");
        }
        if (!"IN_PROGRESS".equals(t.getStatus())) {
            throw new IllegalArgumentException("Tournament is not in progress");
        }

        if (!"A".equals(result) && !"B".equals(result) && !"DRAW".equals(result)) {
            throw new IllegalArgumentException("Result must be A, B, or DRAW");
        }
        if ("DRAW".equals(result) && "ELIMINATION".equals(t.getFormat())) {
            throw new IllegalArgumentException("Draws are not allowed in elimination format");
        }

        // Determine winner
        TournamentTeam winner = null;
        TournamentTeam loser  = null;
        if ("A".equals(result)) {
            winner = match.getTeamA();
            loser  = match.getTeamB();
        } else if ("B".equals(result)) {
            winner = match.getTeamB();
            loser  = match.getTeamA();
        }
        // DRAW: winner stays null

        match.setWinnerTeam(winner);
        match.setStatus("FINISHED");
        if (note != null && !note.isBlank()) {
            match.setNote(note.strip());
        }
        matchRepository.save(match);

        // Update team stats (win=3pts, draw=1pt, loss=0pts)
        if ("DRAW".equals(result)) {
            updateTeamStats(match.getTeamA(), 1, 0, 1, 0);
            updateTeamStats(match.getTeamB(), 1, 0, 1, 0);
        } else {
            updateTeamStats(winner, 3, 1, 0, 0);
            updateTeamStats(loser,  0, 0, 0, 1);
        }

        // Check if current round is complete
        int round = match.getRound();
        List<TeamTournamentMatch> roundMatches = matchRepository.findByTournamentAndRound(t, round);
        boolean roundComplete = roundMatches.stream()
                .allMatch(m -> "FINISHED".equals(m.getStatus()));

        if (roundComplete) {
            if ("ELIMINATION".equals(t.getFormat())) {
                handleEliminationRoundComplete(t, round, roundMatches);
            } else {
                handleRoundRobinComplete(t);
            }
        }

        return match;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TeamTournament> getActive() {
        return tournamentRepository.findActive();
    }

    @Transactional(readOnly = true)
    public TeamTournamentDto getDetail(Long id) {
        TeamTournament t = loadTournament(id);
        List<TournamentTeam> teams = teamRepository.findByTournament(t);
        List<TeamTournamentMatch> matches =
                matchRepository.findByTournamentOrderByRoundAscMatchIndexAsc(t);

        // Batch load all members for all teams
        List<TournamentTeamMember> allMembers = memberRepository.findByTeamIn(teams);
        Map<Long, List<TeamTournamentDto.MemberDto>> membersByTeamId = allMembers.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getTeam().getId(),
                        Collectors.mapping(
                                m -> new TeamTournamentDto.MemberDto(
                                        m.getUser().getId(), m.getUser().getUsername()),
                                Collectors.toList())
                ));

        List<TeamTournamentDto.TeamDto> teamDtos = teams.stream().map(team ->
                new TeamTournamentDto.TeamDto(
                        team.getId(),
                        team.getName(),
                        team.getCaptain().getUsername(),
                        team.getSeed(),
                        team.isEliminated(),
                        team.getPoints(),
                        team.getWins(),
                        team.getDraws(),
                        team.getLosses(),
                        membersByTeamId.getOrDefault(team.getId(), List.of())
                )
        ).toList();

        List<TeamTournamentDto.MatchDto> matchDtos = matches.stream().map(m -> {
            boolean draw = "FINISHED".equals(m.getStatus()) && m.getWinnerTeam() == null;
            return new TeamTournamentDto.MatchDto(
                    m.getId(),
                    m.getRound(),
                    m.getMatchIndex(),
                    m.getTeamA() != null ? m.getTeamA().getName() : null,
                    m.getTeamB() != null ? m.getTeamB().getName() : null,
                    m.getWinnerTeam() != null ? m.getWinnerTeam().getName() : null,
                    draw,
                    m.getStatus(),
                    m.getNote()
            );
        }).toList();

        return new TeamTournamentDto(
                t.getId(),
                t.getName(),
                t.getGameType(),
                t.getTeamCapacity(),
                t.getTeamSizeMax(),
                t.getFormat(),
                t.getStatus(),
                t.getCreator().getUsername(),
                t.getWinnerTeam() != null ? t.getWinnerTeam().getName() : null,
                t.getCreatedAt(),
                teams.size(),
                teamDtos,
                matchDtos
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private TeamTournament loadTournament(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
    }

    private TournamentTeam loadTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
    }

    /** Update standing stats for a team. */
    private void updateTeamStats(TournamentTeam team, int points, int wins, int draws, int losses) {
        team.setPoints(team.getPoints() + points);
        team.setWins(team.getWins() + wins);
        team.setDraws(team.getDraws() + draws);
        team.setLosses(team.getLosses() + losses);
        teamRepository.save(team);
    }

    /** Creates elimination matches for a round and pins each one on the map. */
    private void createEliminationRound(TeamTournament t, int round, List<TournamentTeam> teams,
                                         double latitude, double longitude) {
        List<TeamTournamentMatch> created = new ArrayList<>();
        for (int i = 0; i < teams.size(); i += 2) {
            TournamentTeam teamA = teams.get(i);
            TournamentTeam teamB = teams.get(i + 1);
            TeamTournamentMatch match = new TeamTournamentMatch(t, round, i / 2, teamA, teamB);
            try {
                gameEventService.createEvent(
                        teamA.getCaptain(), latitude, longitude,
                        GameType.valueOf(t.getGameType()),
                        LocalDateTime.now().plusHours(24),
                        "Týmový turnaj: " + t.getName() + " (kolo " + round + ")",
                        null, teamB.getCaptain().getUsername(), false, "TOURNAMENT");
            } catch (Exception ignored) { }
            created.add(match);
        }
        matchRepository.saveAll(created);
    }

    /** Round-robin schedule generator — creates a GameEvent for each match. */
    private void createRoundRobinMatches(TeamTournament t, List<TournamentTeam> teams,
                                          double latitude, double longitude) {
        List<TournamentTeam> circle = new ArrayList<>(teams);
        int n = circle.size();
        if (n % 2 != 0) { circle.add(null); n++; } // null = bye
        int totalRounds = n - 1;
        List<TeamTournamentMatch> allMatches = new ArrayList<>();
        for (int round = 1; round <= totalRounds; round++) {
            int matchIdx = 0;
            for (int i = 0; i < n / 2; i++) {
                TournamentTeam home = circle.get(i);
                TournamentTeam away = circle.get(n - 1 - i);
                if (home != null && away != null) {
                    TeamTournamentMatch match = new TeamTournamentMatch(t, round, matchIdx++, home, away);
                    try {
                        gameEventService.createEvent(
                                home.getCaptain(), latitude, longitude,
                                GameType.valueOf(t.getGameType()),
                                LocalDateTime.now().plusHours(24),
                                "Týmový turnaj: " + t.getName() + " (kolo " + round + ")",
                                null, away.getCaptain().getUsername(), false, "TOURNAMENT");
                    } catch (Exception ignored) { }
                    allMatches.add(match);
                }
            }
            // Rotate: keep position 0 fixed, insert last element at position 1
            TournamentTeam last = circle.remove(n - 1);
            circle.add(1, last);
        }
        matchRepository.saveAll(allMatches);
    }

    /** Called when all matches in an elimination round are finished. */
    private void handleEliminationRoundComplete(TeamTournament t, int round,
                                                 List<TeamTournamentMatch> roundMatches) {
        List<TournamentTeam> winners = roundMatches.stream()
                .map(TeamTournamentMatch::getWinnerTeam)
                .toList();

        // Mark losers as eliminated
        roundMatches.forEach(m -> {
            TournamentTeam loser = m.getTeamA().getId().equals(
                    m.getWinnerTeam().getId()) ? m.getTeamB() : m.getTeamA();
            loser.setEliminated(true);
            teamRepository.save(loser);
        });

        if (winners.size() == 1) {
            // Final decided — tournament over
            t.setWinnerTeam(winners.get(0));
            t.setStatus("FINISHED");
            tournamentRepository.save(t);
        } else {
            // Advance winners to next round (reuse stored tournament location)
            createEliminationRound(t, round + 1, winners, t.getLatitude(), t.getLongitude());
        }
    }

    /** Called when all matches in the round-robin are finished. */
    private void handleRoundRobinComplete(TeamTournament t) {
        // Check if any match is still pending
        List<TeamTournamentMatch> allMatches =
                matchRepository.findByTournamentOrderByRoundAscMatchIndexAsc(t);
        boolean allDone = allMatches.stream().allMatch(m -> "FINISHED".equals(m.getStatus()));
        if (!allDone) {
            return; // Still matches remaining in other rounds
        }

        // Determine winner: highest points, tiebreak by wins
        List<TournamentTeam> standings =
                teamRepository.findByTournamentOrderByPointsDescWinsDesc(t);
        if (!standings.isEmpty()) {
            t.setWinnerTeam(standings.get(0));
        }
        t.setStatus("FINISHED");
        tournamentRepository.save(t);
    }
}
