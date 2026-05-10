<template>
  <div class="tt-detail-page">

    <!-- ── Back link ──────────────────────────────────────────────────────── -->
    <router-link to="/tournaments" class="back-link">{{ $t('teamTournament.back') }}</router-link>

    <!-- ── Loading / Error ─────────────────────────────────────────────────── -->
    <div v-if="loading" class="state-msg">{{ $t('common.loading') }}</div>
    <div v-else-if="error" class="state-msg err">{{ error }}</div>

    <template v-else-if="tournament">

      <!-- ── Header ──────────────────────────────────────────────────────────── -->
      <div class="tt-header">
        <div class="tt-title-row">
          <h2>{{ tournament.name }}</h2>
          <div class="badge-row">
            <span class="badge format">
              {{ tournament.format === 'ELIMINATION'
                ? $t('teamTournament.formatElimination')
                : $t('teamTournament.formatRoundRobin') }}
            </span>
            <span class="badge status" :class="tournament.status.toLowerCase()">
              {{ $t(`teamTournament.${tournament.status.toLowerCase()}`) }}
            </span>
          </div>
        </div>
        <div class="tt-meta">
          <span>{{ gameIcon(tournament.gameType) }} {{ gameLabel(tournament.gameType) }}</span>
          <span>{{ $t('teamTournament.createdBy') }} {{ tournament.creatorUsername }}</span>
          <span>{{ $t('teamTournament.waitingForTeams', { count: tournament.teamCount, capacity: tournament.teamCapacity }) }}</span>
          <span>{{ $t('teamTournament.teamSizeLabel') }}: max {{ tournament.teamSizeMax }}</span>
        </div>
      </div>

      <!-- ── Winner banner ───────────────────────────────────────────────────── -->
      <div v-if="tournament.status === 'FINISHED' && tournament.winnerTeamName" class="winner-banner">
        {{ $t('teamTournament.winnerTeam', { name: tournament.winnerTeamName }) }}
      </div>

      <!-- ── Start button (creator, OPEN, ≥2 teams) ──────────────────────────── -->
      <div v-if="isCreator && tournament.status === 'OPEN' && tournament.teamCount >= 2" class="start-row">
        <p v-if="startError" class="msg-error">{{ startError }}</p>
        <button class="btn-primary" :disabled="starting" @click="startTournament">
          {{ starting ? $t('teamTournament.starting') : $t('teamTournament.startBtn') }}
        </button>
      </div>

      <!-- ── Teams section ───────────────────────────────────────────────────── -->
      <section class="section">
        <h3 class="section-title">{{ $t('teamTournament.teamsSection') }}</h3>

        <!-- Create team form (OPEN and user not yet in any team) -->
        <div v-if="tournament.status === 'OPEN' && !myTeam && currentUser" class="create-team-form">
          <input
            v-model="newTeamName"
            type="text"
            maxlength="60"
            :placeholder="$t('teamTournament.createTeamPlaceholder')"
          />
          <button class="btn-primary" :disabled="creatingTeam || !newTeamName.trim()" @click="createTeam">
            {{ creatingTeam ? $t('common.saving') : $t('teamTournament.createTeamSubmit') }}
          </button>
          <p v-if="createTeamError" class="msg-error">{{ createTeamError }}</p>
        </div>

        <!-- Team cards -->
        <div class="teams-grid">
          <div
            v-for="team in tournament.teams"
            :key="team.id"
            class="team-card"
            :class="{ eliminated: team.eliminated, 'my-team': myTeam && myTeam.id === team.id }"
          >
            <div class="team-card-top">
              <div class="team-name-row">
                <span class="team-name">{{ team.name }}</span>
                <span v-if="team.seed" class="team-seed">#{{ team.seed }}</span>
                <span v-if="team.eliminated" class="elim-badge">{{ $t('teamTournament.eliminated') }}</span>
              </div>
              <div class="team-stats">
                <span class="stat-pts">{{ team.points }}pt</span>
                <span class="stat">{{ team.wins }}W</span>
                <span class="stat draw">{{ team.draws }}D</span>
                <span class="stat loss">{{ team.losses }}L</span>
              </div>
            </div>

            <div class="team-members">
              <span
                v-for="member in team.members"
                :key="member.userId"
                class="member-chip"
                :class="{ captain: member.username === team.captainUsername }"
              >
                {{ member.username }}<span v-if="member.username === team.captainUsername" class="captain-tag">{{ $t('teamTournament.captain') }}</span>
                <button
                  v-if="tournament.status === 'OPEN' && currentUser === team.captainUsername && member.username !== currentUser"
                  class="btn-kick"
                  :disabled="kickingMember === member.userId"
                  :title="$t('teamTournament.kickMember')"
                  @click.stop="kickMember(team, member)"
                >×</button>
              </span>
            </div>

            <!-- Join button (OPEN, user not in any team) -->
            <div v-if="tournament.status === 'OPEN' && !myTeam && currentUser" class="team-actions">
              <button
                class="btn-sm btn-outline"
                :disabled="joiningTeam === team.id"
                @click="joinTeam(team)"
              >
                {{ joiningTeam === team.id ? $t('common.saving') : $t('teamTournament.joinTeam') }}
              </button>
            </div>

            <!-- Leave button (member, not captain) -->
            <div
              v-if="tournament.status === 'OPEN' && myTeam && myTeam.id === team.id && currentUser !== team.captainUsername"
              class="team-actions"
            >
              <button class="btn-sm btn-danger" :disabled="leavingTeam" @click="leaveTeam(team)">
                {{ leavingTeam ? $t('common.saving') : $t('teamTournament.leaveTeam') }}
              </button>
            </div>
          </div>
        </div>
        <p v-if="teamActionError" class="msg-error section-error">{{ teamActionError }}</p>
      </section>

      <!-- ── Standings table (ROUND_ROBIN) ─────────────────────────────────────── -->
      <section v-if="tournament.format === 'ROUND_ROBIN' && tournament.teams.length" class="section">
        <h3 class="section-title">{{ $t('teamTournament.standings') }}</h3>
        <table class="standings-table">
          <thead>
            <tr>
              <th>#</th>
              <th>{{ $t('teamTournament.teamsSection') }}</th>
              <th>W</th>
              <th>D</th>
              <th>L</th>
              <th>Pts</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(team, idx) in sortedTeams"
              :key="team.id"
              :class="{
                'my-team-row': myTeam && myTeam.id === team.id,
                'winner-row': idx === 0 && tournament.status === 'FINISHED'
              }"
            >
              <td class="rank-col">{{ idx + 1 }}</td>
              <td class="name-col">
                {{ team.name }}
                <span v-if="tournament.winnerTeamName === team.name" class="winner-crown">★</span>
              </td>
              <td>{{ team.wins }}</td>
              <td>{{ team.draws }}</td>
              <td>{{ team.losses }}</td>
              <td class="pts-col">{{ team.points }}</td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- ── Matches section ─────────────────────────────────────────────────── -->
      <section v-if="tournament.matches && tournament.matches.length" class="section">
        <h3 class="section-title">{{ $t('teamTournament.matchesSection') }}</h3>

        <div v-for="(roundMatches, round) in matchesByRound" :key="round" class="round-block">
          <h4 class="round-title">{{ $t('teamTournament.round', { n: round }) }}</h4>

          <div class="match-list">
            <div
              v-for="match in roundMatches"
              :key="match.id"
              class="match-row"
              :class="{ finished: match.status === 'FINISHED' }"
            >
              <!-- Teams + result -->
              <div class="match-teams">
                <span
                  class="match-team team-a"
                  :class="{
                    winner: match.status === 'FINISHED' && !match.draw && match.winnerTeamName === match.teamAName,
                    loser:  match.status === 'FINISHED' && !match.draw && match.winnerTeamName !== match.teamAName
                  }"
                >{{ match.teamAName }}</span>
                <span class="match-vs">{{ $t('teamTournament.vs') }}</span>
                <span
                  class="match-team team-b"
                  :class="{
                    winner: match.status === 'FINISHED' && !match.draw && match.winnerTeamName === match.teamBName,
                    loser:  match.status === 'FINISHED' && !match.draw && match.winnerTeamName !== match.teamBName
                  }"
                >{{ match.teamBName }}</span>
              </div>

              <!-- Result label -->
              <div class="match-result">
                <template v-if="match.status === 'FINISHED'">
                  <span v-if="match.draw" class="result-draw">{{ $t('teamTournament.draw') }}</span>
                  <span v-else class="result-winner">{{ match.winnerTeamName }}</span>
                  <span v-if="match.note" class="match-note"> — {{ match.note }}</span>
                </template>
                <span v-else class="result-pending">{{ $t('teamTournament.pending') }}</span>
              </div>

              <!-- Record result panel (creator only, PENDING match, IN_PROGRESS tournament) -->
              <div
                v-if="isCreator && match.status === 'PENDING' && tournament.status === 'IN_PROGRESS'"
                class="record-result"
              >
                <div class="result-btns">
                  <button class="btn-result btn-a" @click="recordResult(match, 'A')">
                    {{ match.teamAName }}
                  </button>
                  <button
                    v-if="tournament.format === 'ROUND_ROBIN'"
                    class="btn-result btn-draw"
                    @click="recordResult(match, 'DRAW')"
                  >{{ $t('teamTournament.draw') }}</button>
                  <button class="btn-result btn-b" @click="recordResult(match, 'B')">
                    {{ match.teamBName }}
                  </button>
                </div>
                <div class="result-note-row">
                  <input
                    v-model="matchNotes[match.id]"
                    type="text"
                    class="note-input"
                    :placeholder="$t('map.notePlaceholder')"
                  />
                </div>
                <p v-if="recordErrors[match.id]" class="msg-error">{{ recordErrors[match.id] }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

    </template>
  </div>
</template>

<script>
import axios from 'axios'
import { useI18n } from 'vue-i18n'

export default {
  name: 'TeamTournamentDetailPage',

  setup() {
    const { locale } = useI18n()
    return { locale }
  },

  data() {
    return {
      tournament:  null,
      gameMeta:    {},
      loading:     true,
      error:       '',
      currentUser: localStorage.getItem('username') || '',

      // Start tournament
      starting:   false,
      startError: '',

      // Create team
      newTeamName:     '',
      creatingTeam:    false,
      createTeamError: '',

      // Join / leave / kick
      joiningTeam:     null,  // teamId currently being joined
      leavingTeam:     false,
      kickingMember:   null,  // userId currently being kicked
      teamActionError: '',

      // Record match result
      matchNotes:   {},  // matchId → note string
      recordErrors: {},  // matchId → error string
    }
  },

  computed: {
    currentLocale() { return this.locale },

    isCreator() {
      return !!this.tournament && this.tournament.creatorUsername === this.currentUser
    },

    /** The team the current user belongs to, or null. */
    myTeam() {
      if (!this.tournament || !this.currentUser) return null
      return this.tournament.teams.find(t =>
        t.members.some(m => m.username === this.currentUser)
      ) ?? null
    },

    /** Teams sorted by points desc, then wins desc (for standings table). */
    sortedTeams() {
      if (!this.tournament) return []
      return [...this.tournament.teams].sort((a, b) =>
        b.points !== a.points ? b.points - a.points : b.wins - a.wins
      )
    },

    /** Matches grouped by round number, each group sorted by matchIndex. */
    matchesByRound() {
      if (!this.tournament?.matches) return {}
      const grouped = {}
      for (const m of this.tournament.matches) {
        if (!grouped[m.round]) grouped[m.round] = []
        grouped[m.round].push(m)
      }
      for (const r of Object.keys(grouped)) {
        grouped[r].sort((a, b) => a.matchIndex - b.matchIndex)
      }
      return grouped
    },
  },

  async mounted() {
    await Promise.all([this.loadGames(), this.loadTournament()])
  },

  methods: {
    async loadGames() {
      try {
        const { data } = await axios.get('/api/games', { withCredentials: true })
        data.forEach(g => { this.gameMeta[g.key] = g })
      } catch { /* non-fatal */ }
    },

    async loadTournament() {
      this.loading = true
      this.error   = ''
      try {
        const { data } = await axios.get(
          `/api/team-tournaments/${this.$route.params.id}`,
          { withCredentials: true }
        )
        this.tournament = data
      } catch {
        this.error = this.$t('teamTournament.couldNotLoad')
      } finally {
        this.loading = false
      }
    },

    async startTournament() {
      this.starting   = true
      this.startError = ''
      // Location is required — every match must appear on the map
      const pos = await this.getPosition()
      if (!pos) {
        this.startError = this.$t('teamTournament.locationRequired')
        this.starting = false
        return
      }
      try {
        const { data } = await axios.post(
          `/api/team-tournaments/${this.tournament.id}/start`,
          { latitude: pos.lat, longitude: pos.lng },
          { withCredentials: true }
        )
        this.tournament = data
      } catch (err) {
        this.startError = err.response?.data || this.$t('teamTournament.couldNotStart')
      } finally {
        this.starting = false
      }
    },

    getPosition() {
      return new Promise(resolve => {
        if (!navigator.geolocation) { resolve(null); return }
        const timer = setTimeout(() => resolve(null), 6000)
        navigator.geolocation.getCurrentPosition(
          p  => { clearTimeout(timer); resolve({ lat: p.coords.latitude, lng: p.coords.longitude }) },
          () => { clearTimeout(timer); resolve(null) }
        )
      })
    },

    async createTeam() {
      if (!this.newTeamName.trim()) return
      this.creatingTeam    = true
      this.createTeamError = ''
      try {
        const { data } = await axios.post(
          `/api/team-tournaments/${this.tournament.id}/teams`,
          { name: this.newTeamName.trim() },
          { withCredentials: true }
        )
        this.tournament  = data
        this.newTeamName = ''
      } catch (err) {
        this.createTeamError = err.response?.data || this.$t('teamTournament.couldNotCreateTeam')
      } finally {
        this.creatingTeam = false
      }
    },

    async joinTeam(team) {
      this.joiningTeam    = team.id
      this.teamActionError = ''
      try {
        const { data } = await axios.post(
          `/api/team-tournaments/${this.tournament.id}/teams/${team.id}/join`,
          {},
          { withCredentials: true }
        )
        this.tournament = data
      } catch (err) {
        this.teamActionError = err.response?.data || this.$t('teamTournament.couldNotJoin')
      } finally {
        this.joiningTeam = null
      }
    },

    async leaveTeam(team) {
      this.leavingTeam    = true
      this.teamActionError = ''
      try {
        const { data } = await axios.delete(
          `/api/team-tournaments/${this.tournament.id}/teams/${team.id}/leave`,
          { withCredentials: true }
        )
        this.tournament = data
      } catch (err) {
        this.teamActionError = err.response?.data || this.$t('teamTournament.couldNotLeave')
      } finally {
        this.leavingTeam = false
      }
    },

    async kickMember(team, member) {
      this.kickingMember   = member.userId
      this.teamActionError = ''
      try {
        const { data } = await axios.delete(
          `/api/team-tournaments/${this.tournament.id}/teams/${team.id}/members/${member.userId}`,
          { withCredentials: true }
        )
        this.tournament = data
      } catch (err) {
        this.teamActionError = err.response?.data || this.$t('teamTournament.couldNotKick')
      } finally {
        this.kickingMember = null
      }
    },

    async recordResult(match, result) {
      this.recordErrors = { ...this.recordErrors, [match.id]: '' }
      try {
        const { data } = await axios.post(
          `/api/team-tournaments/${this.tournament.id}/matches/${match.id}/result`,
          { result, note: this.matchNotes[match.id] || null },
          { withCredentials: true }
        )
        this.tournament = data
        const notes = { ...this.matchNotes }
        delete notes[match.id]
        this.matchNotes = notes
      } catch (err) {
        const msg = err.response?.data || this.$t('teamTournament.couldNotRecord')
        this.recordErrors = { ...this.recordErrors, [match.id]: msg }
      }
    },

    gameIcon(key)  { return this.gameMeta[key]?.icon || '' },
    gameLabel(key) {
      const g = this.gameMeta[key]
      if (!g) return key || ''
      return this.currentLocale === 'cs' ? g.nameCs : g.nameEn
    },
  },
}
</script>

<style scoped>
.tt-detail-page {
  max-width: 760px;
  margin: 0 auto;
  padding: 28px 20px 72px;
}

/* ── Back link ───────────────────────────────────────────────────────── */
.back-link {
  display: inline-block;
  font-size: 13px;
  color: var(--text-muted);
  text-decoration: none;
  margin-bottom: 22px;
  transition: color var(--transition);
}
.back-link:hover { color: var(--brand); }

/* ── State messages ──────────────────────────────────────────────────── */
.state-msg     { text-align: center; padding: 60px 20px; color: var(--text-muted); font-size: 14px; }
.state-msg.err { color: var(--red); }

/* ── Header ──────────────────────────────────────────────────────────── */
.tt-header { margin-bottom: 22px; }

.tt-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}
h2 { font-size: 22px; font-weight: 700; margin: 0; letter-spacing: -.01em; color: var(--text-primary); }

.badge-row { display: flex; gap: 6px; align-items: center; flex-wrap: wrap; flex-shrink: 0; }
.badge {
  font-size: 10px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .05em; padding: 3px 10px; border-radius: var(--r-full);
}
.badge.format { background: var(--bg-overlay); color: var(--text-secondary); }
.badge.status.open        { background: var(--brand-muted); color: var(--brand); }
.badge.status.in_progress { background: var(--blue-muted);  color: var(--blue); }
.badge.status.finished    { background: rgba(139,148,158,.12); color: var(--text-muted); }

.tt-meta {
  display: flex; flex-wrap: wrap; gap: 6px 20px;
  font-size: 12px; color: var(--text-secondary);
}

/* ── Winner banner ───────────────────────────────────────────────────── */
.winner-banner {
  background: linear-gradient(135deg, var(--brand-muted), var(--blue-muted));
  border: 1px solid var(--brand);
  border-radius: var(--r-md);
  padding: 14px 20px;
  font-size: 16px; font-weight: 700;
  color: var(--brand); text-align: center;
  margin-bottom: 22px;
}

/* ── Start row ───────────────────────────────────────────────────────── */
.start-row {
  display: flex; flex-direction: column; align-items: flex-start;
  gap: 8px; margin-bottom: 24px;
}

/* ── Section ─────────────────────────────────────────────────────────── */
.section { margin-bottom: 34px; }
.section-title {
  font-size: 11px; font-weight: 700; color: var(--text-muted);
  text-transform: uppercase; letter-spacing: .06em; margin: 0 0 14px;
}
.section-error { margin-top: 8px; }

/* ── Create team form ────────────────────────────────────────────────── */
.create-team-form {
  display: flex; gap: 8px; align-items: center; flex-wrap: wrap;
  margin-bottom: 16px;
}
.create-team-form input {
  flex: 1; min-width: 160px;
  background: var(--bg-elevated); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--r);
  padding: 7px 11px; font-size: 13px; font-family: var(--font); outline: none;
  transition: border-color var(--transition);
}
.create-team-form input:focus { border-color: var(--brand); }
.create-team-form input::placeholder { color: var(--text-muted); }

/* ── Team cards ──────────────────────────────────────────────────────── */
.teams-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 12px;
}
.team-card {
  background: var(--bg-surface); border: 1px solid var(--border);
  border-radius: var(--r-md); padding: 14px 16px;
  transition: background var(--transition), border-color var(--transition);
}
.team-card.my-team    { border-color: var(--brand); }
.team-card.eliminated { opacity: .5; }

.team-card-top {
  display: flex; justify-content: space-between;
  align-items: flex-start; gap: 8px; margin-bottom: 10px;
}
.team-name-row { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; flex: 1; }
.team-name  { font-size: 14px; font-weight: 700; color: var(--text-primary); }
.team-seed  { font-size: 11px; color: var(--text-muted); }
.elim-badge {
  font-size: 9px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .04em; color: var(--red);
  background: rgba(220,53,69,.1); border-radius: var(--r-full);
  padding: 1px 6px;
}
.team-stats {
  display: flex; gap: 5px; align-items: center;
  font-size: 11px; flex-shrink: 0;
}
.stat-pts { font-weight: 700; color: var(--brand); font-size: 12px; }
.stat      { color: var(--text-secondary); }
.stat.draw { color: var(--text-muted); }
.stat.loss { color: var(--red); }

.team-members { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 10px; }
.member-chip {
  display: flex; align-items: center; gap: 4px;
  font-size: 11px; background: var(--bg-elevated);
  border: 1px solid var(--border); border-radius: var(--r);
  padding: 2px 7px; color: var(--text-secondary);
}
.member-chip.captain {
  border-color: rgba(66,184,131,.4);
  color: var(--text-primary); font-weight: 600;
}
.captain-tag {
  font-size: 9px; color: var(--brand); font-weight: 700;
  text-transform: uppercase; letter-spacing: .03em;
}
.btn-kick {
  background: none; border: none; color: var(--red);
  cursor: pointer; font-size: 13px; padding: 0 2px;
  opacity: 0.6; line-height: 1; flex-shrink: 0;
  transition: opacity var(--transition);
}
.btn-kick:hover:not(:disabled) { opacity: 1; }
.btn-kick:disabled { opacity: 0.25; cursor: default; }
.team-actions { margin-top: 2px; }

/* ── Standings table ─────────────────────────────────────────────────── */
.standings-table {
  width: 100%; border-collapse: collapse; font-size: 13px;
}
.standings-table th {
  text-align: left; font-size: 10px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .04em;
  color: var(--text-muted); padding: 6px 10px;
  border-bottom: 1px solid var(--border);
}
.standings-table td {
  padding: 9px 10px; border-bottom: 1px solid var(--border);
  color: var(--text-primary);
}
.standings-table tbody tr:hover { background: var(--bg-elevated); }
.my-team-row td { color: var(--brand); }
.winner-row .name-col { font-weight: 700; }
.rank-col { color: var(--text-muted); width: 28px; }
.name-col { font-weight: 600; }
.pts-col  { font-weight: 700; color: var(--brand); }
.winner-crown { color: #c9a227; margin-left: 4px; }

/* ── Rounds & matches ────────────────────────────────────────────────── */
.round-block  { margin-bottom: 24px; }
.round-title  {
  font-size: 11px; font-weight: 700; color: var(--text-muted);
  text-transform: uppercase; letter-spacing: .05em; margin: 0 0 10px;
}
.match-list   { display: flex; flex-direction: column; gap: 8px; }

.match-row {
  background: var(--bg-surface); border: 1px solid var(--border);
  border-radius: var(--r-md); padding: 12px 16px;
  transition: background var(--transition);
}
.match-row.finished { background: var(--bg-elevated); }

.match-teams {
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 6px; flex-wrap: wrap;
}
.match-team {
  font-size: 14px; font-weight: 600; color: var(--text-primary);
  flex: 1; text-align: center;
}
.match-team.winner { color: var(--brand); }
.match-team.loser  { color: var(--text-muted); text-decoration: line-through; }
.match-vs {
  font-size: 11px; font-weight: 700; color: var(--text-muted); flex-shrink: 0;
}

.match-result {
  font-size: 12px; color: var(--text-secondary);
  text-align: center; margin-bottom: 2px;
}
.result-winner { font-weight: 700; color: var(--brand); }
.result-draw   { font-weight: 600; color: var(--blue); }
.result-pending { color: var(--text-muted); font-style: italic; }
.match-note    { color: var(--text-muted); }

/* ── Record result panel ─────────────────────────────────────────────── */
.record-result { margin-top: 10px; display: flex; flex-direction: column; gap: 6px; }
.result-btns   { display: flex; gap: 6px; flex-wrap: wrap; justify-content: center; }
.btn-result {
  flex: 1; min-width: 80px;
  padding: 6px 10px; border: none; border-radius: var(--r);
  font-size: 12px; font-weight: 600; cursor: pointer;
  font-family: var(--font); transition: opacity var(--transition), background var(--transition);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.btn-a    { background: var(--brand-muted); color: var(--brand); }
.btn-a:hover    { background: var(--brand); color: #fff; }
.btn-b    { background: var(--blue-muted);  color: var(--blue); }
.btn-b:hover    { background: var(--blue);  color: #fff; }
.btn-draw { background: var(--bg-overlay);  color: var(--text-secondary); }
.btn-draw:hover { background: var(--border); color: var(--text-primary); }

.result-note-row { display: flex; }
.note-input {
  flex: 1; background: var(--bg-elevated); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--r);
  padding: 5px 9px; font-size: 12px; font-family: var(--font); outline: none;
  transition: border-color var(--transition);
}
.note-input:focus { border-color: var(--brand); }
.note-input::placeholder { color: var(--text-muted); }

/* ── Shared buttons ──────────────────────────────────────────────────── */
.btn-primary {
  background: var(--brand); color: #fff; border: none; border-radius: var(--r);
  padding: 7px 18px; font-weight: 600; cursor: pointer;
  font-family: var(--font); font-size: 13px; white-space: nowrap;
  transition: background var(--transition);
}
.btn-primary:hover:not(:disabled) { background: var(--brand-hover); }
.btn-primary:disabled { opacity: .45; cursor: default; }

.btn-sm {
  font-size: 11px; padding: 4px 12px; border-radius: var(--r);
  cursor: pointer; font-family: var(--font); font-weight: 600; border: none;
  transition: all var(--transition);
}
.btn-outline {
  background: var(--bg-elevated); color: var(--text-primary);
  border: 1px solid var(--border) !important;
}
.btn-outline:hover:not(:disabled) { border-color: var(--brand) !important; color: var(--brand); }
.btn-danger  { background: rgba(220,53,69,.12); color: var(--red); }
.btn-danger:hover:not(:disabled)  { background: var(--red); color: #fff; }
.btn-sm:disabled { opacity: .4; cursor: default; }

.msg-error { color: var(--red); font-size: 12px; margin: 0; }

@media (max-width: 600px) {
  .tt-detail-page { padding: 18px 12px 76px; }
  .teams-grid     { grid-template-columns: 1fr; }
  h2              { font-size: 18px; }
  .match-team     { font-size: 13px; }
}
</style>
